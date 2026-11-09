package com.apigestaofinanceira.transacoes.application.usecases;

import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.ContaClientPort;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import com.apigestaofinanceira.transacoes.domain.ports.output.UsuarioClientPort;
import com.apigestaofinanceira.transacoes.infrastructure.config.exception.UsuarioNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreateTransacaoUseCase {

    private final TransacaoRepositoryPort transacaoRepository;
    private final UsuarioClientPort usuarioClient;
    private final ContaClientPort contaClient;

    public CreateTransacaoUseCase(TransacaoRepositoryPort transacaoRepository, UsuarioClientPort usuarioClient, ContaClientPort contaClient) {

        this.transacaoRepository = transacaoRepository;
        this.usuarioClient = usuarioClient;
        this.contaClient = contaClient;
    }

    public Transacao executar(Transacao transacao) {

        validarRegras(transacao);

        if (!usuarioClient.usuarioExiste(transacao.getUsuarioId())) {
            throw new UsuarioNaoEncontradoException(transacao.getUsuarioId());
        }

        if (transacao.getUsuarioDestinoId() != null) {
            if (!usuarioClient.usuarioExiste(transacao.getUsuarioDestinoId())) {
                throw new IllegalArgumentException("Usuário destinatário não encontrado.");
            }
        }

        if (transacao.getDataHora() == null) {
            transacao.setDataHora(LocalDateTime.now());
        }

        // Consulta a conta de origem para conhecer o saldo atual.
        ContaClientPort.ContaDados contaOrigem = contaClient.consultarConta(transacao.getUsuarioId());

        // Depósitos não precisam passar pela validação de limites.
        if (transacao.getTipo() != TipoTransacao.DEPOSITO) {

            String motivoRejeicao = verificarLimites(transacao, contaOrigem);

            if (motivoRejeicao != null) {
                transacao.setStatus(StatusTransacao.REJEITADA);
                transacao.setMotivoRejeicao(motivoRejeicao);

                return transacaoRepository.salvar(transacao);
            }
        }

        // Em transferências, verifica se a conta de destino existe.
        ContaClientPort.ContaDados contaDestino = null;

        if (transacao.getTipo() == TipoTransacao.TRANSFERENCIA) {

            try {
                contaDestino = contaClient.consultarConta(transacao.getUsuarioDestinoId());
            } catch (IllegalArgumentException e) {
                transacao.setStatus(StatusTransacao.REJEITADA);
                transacao.setMotivoRejeicao("Conta do usuário destinatário não encontrada.");

                return transacaoRepository.salvar(transacao);
            }
        }

        // A transação foi aprovada.
        transacao.setStatus(StatusTransacao.APROVADA);
        transacao.setMotivoRejeicao(null);

        BigDecimal valor = transacao.getValor();

        // Atualiza o saldo conforme o tipo da transação.
        switch (transacao.getTipo()) {

            case DEPOSITO -> {
                BigDecimal novoSaldo = contaOrigem.saldo().add(valor);

                contaClient.atualizarSaldo(transacao.getUsuarioId(), novoSaldo);
            }

            case SAQUE, COMPRA -> {
                BigDecimal novoSaldo = contaOrigem.saldo().subtract(valor);

                contaClient.atualizarSaldo(transacao.getUsuarioId(), novoSaldo);
            }

            case TRANSFERENCIA -> {

                BigDecimal novoSaldoOrigem = contaOrigem.saldo().subtract(valor);

                BigDecimal novoSaldoDestino = contaDestino.saldo().add(valor);


                contaClient.atualizarSaldo(transacao.getUsuarioId(), novoSaldoOrigem);

                try {


                    contaClient.atualizarSaldo(transacao.getUsuarioDestinoId(), novoSaldoDestino);

                } catch (RuntimeException erroCredito) {

                    try {

                        contaClient.atualizarSaldo(transacao.getUsuarioId(), contaOrigem.saldo());

                    } catch (RuntimeException erroCompensacao) {

                        IllegalStateException erro = new IllegalStateException("Falha no crédito da transferência e também na " + "compensação do débito. " + "É necessária conciliação manual.", erroCredito);

                        erro.addSuppressed(erroCompensacao);

                        throw erro;
                    }

                    transacao.setStatus(StatusTransacao.REJEITADA);
                    transacao.setMotivoRejeicao("Falha na transferência, débito compensado.");

                    transacaoRepository.salvar(transacao);

                    throw new IllegalStateException("Transferência rejeitada. O débito foi compensado.", erroCredito);
                }
            }
        }

        return transacaoRepository.salvar(transacao);
    }

    private String verificarLimites(Transacao transacao, ContaClientPort.ContaDados conta) {

        BigDecimal valor = transacao.getValor();

        if (valor.compareTo(conta.saldo()) > 0) {
            return "Saldo insuficiente.";
        }

        if (valor.compareTo(conta.limitePorTransacao()) > 0) {
            return "O valor ultrapassa o limite por transação.";
        }

        LocalDate dataTransacao = transacao.getDataHora().toLocalDate();

        List<Transacao> transacoesDoUsuario = transacaoRepository.buscarPorUsuarioId(transacao.getUsuarioId());

        BigDecimal totalDoDia = transacoesDoUsuario.stream().filter(t -> t.getStatus() == StatusTransacao.APROVADA).filter(t -> t.getTipo() != TipoTransacao.DEPOSITO).filter(t -> t.getDataHora().toLocalDate().equals(dataTransacao)).map(Transacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalComNovaTransacao = totalDoDia.add(valor);

        if (totalComNovaTransacao.compareTo(conta.limiteDiario()) > 0) {
            return "O limite diário foi ultrapassado.";
        }

        return null;
    }

    private void validarRegras(Transacao transacao) {

        if (transacao.getTipo() == null) {
            throw new IllegalArgumentException("O tipo da transação é obrigatório.");
        }

        if (transacao.getTipo() == TipoTransacao.TRANSFERENCIA) {

            if (transacao.getUsuarioDestinoId() == null) {
                throw new IllegalArgumentException("A transferência exige um usuário destinatário.");
            }

            if (transacao.getUsuarioDestinoId().equals(transacao.getUsuarioId())) {
                throw new IllegalArgumentException("O usuário destinatário deve ser diferente do remetente.");
            }

        } else if (transacao.getUsuarioDestinoId() != null) {

            throw new IllegalArgumentException("Somente transferências podem possuir usuário destinatário.");
        }

        if (transacao.getTipo() == TipoTransacao.COMPRA) {

            if (transacao.getCategoria() == null || transacao.getCategoria().isBlank()) {
                throw new IllegalArgumentException("A categoria é obrigatória para compras.");
            }
        }
    }
}
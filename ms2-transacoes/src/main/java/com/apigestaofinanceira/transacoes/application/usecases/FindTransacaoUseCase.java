package com.apigestaofinanceira.transacoes.application.usecases;

import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FindTransacaoUseCase {

    private final TransacaoRepositoryPort transacaoRepository;

    public FindTransacaoUseCase(
            TransacaoRepositoryPort transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public Optional<Transacao> executar(Long id, Long usuarioId) {

        return transacaoRepository.buscarPorId(id)
                .filter(transacao ->
                        transacao.getUsuarioId().equals(usuarioId));
    }
}
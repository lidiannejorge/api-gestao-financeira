package com.apigestaofinanceira.transacoes.application;

import com.apigestaofinanceira.transacoes.application.usecases.AnalisarDespesasUseCase;
import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AnalisarDespesasUseCaseTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    private AnalisarDespesasUseCase analisarDespesasUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        analisarDespesasUseCase = new AnalisarDespesasUseCase(
                transacaoRepository, Clock.systemDefaultZone());
    }

    private Transacao criarTransacao(
            TipoTransacao tipo,
            BigDecimal valor,
            String categoria,
            StatusTransacao status,
            LocalDateTime dataHora) {

        Transacao transacao = new Transacao();
        transacao.setTipo(tipo);
        transacao.setValor(valor);
        transacao.setCategoria(categoria);
        transacao.setStatus(status);
        transacao.setDataHora(dataHora);
        transacao.setUsuarioId(1L);

        return transacao;
    }

    // 1. Cinco compras em três categorias: resumo com três linhas e total correto.
    @Test
    void deveAgruparCincoComprasEmTresCategorias() {

        LocalDateTime agora = LocalDateTime.now();

        List<Transacao> transacoes = List.of(
                criarTransacao(TipoTransacao.COMPRA, new BigDecimal("100"), "Alimentação", StatusTransacao.APROVADA, agora),
                criarTransacao(TipoTransacao.COMPRA, new BigDecimal("50"), "Alimentação", StatusTransacao.APROVADA, agora),
                criarTransacao(TipoTransacao.COMPRA, new BigDecimal("200"), "Transporte", StatusTransacao.APROVADA, agora),
                criarTransacao(TipoTransacao.COMPRA, new BigDecimal("30"), "Lazer", StatusTransacao.APROVADA, agora),
                criarTransacao(TipoTransacao.COMPRA, new BigDecimal("20"), "Lazer", StatusTransacao.APROVADA, agora)
        );

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(transacoes);

        AnalisarDespesasUseCase.Resultado resultado =
                analisarDespesasUseCase.executar(1L, null, null);

        assertEquals(3, resultado.porCategoria().size());
        assertEquals(new BigDecimal("400"), resultado.totalGeral());
        assertEquals(new BigDecimal("150"), resultado.porCategoria().get("Alimentação"));
        assertEquals(new BigDecimal("200"), resultado.porCategoria().get("Transporte"));
        assertEquals(new BigDecimal("50"), resultado.porCategoria().get("Lazer"));
    }

    // 2. Transações rejeitadas e depósitos não entram no resumo.
    @Test
    void naoDeveContabilizarTransacoesRejeitadasNemDepositos() {

        LocalDateTime agora = LocalDateTime.now();

        List<Transacao> transacoes = List.of(
                criarTransacao(TipoTransacao.COMPRA, new BigDecimal("100"), "Alimentação", StatusTransacao.REJEITADA, agora),
                criarTransacao(TipoTransacao.DEPOSITO, new BigDecimal("500"), null, StatusTransacao.APROVADA, agora),
                criarTransacao(TipoTransacao.SAQUE, new BigDecimal("50"), "Saque", StatusTransacao.APROVADA, agora)
        );

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(transacoes);

        AnalisarDespesasUseCase.Resultado resultado =
                analisarDespesasUseCase.executar(1L, null, null);

        assertEquals(new BigDecimal("50"), resultado.totalGeral());
        assertEquals(1, resultado.porCategoria().size());
    }

    // 3. Sem período informado, usa o mês atual.
    @Test
    void deveUsarMesAtualComoPeriodoPadrao() {

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(List.of());

        AnalisarDespesasUseCase.Resultado resultado =
                analisarDespesasUseCase.executar(1L, null, null);

        assertEquals(YearMonth.now().atDay(1), resultado.inicio());
        assertEquals(YearMonth.now().atEndOfMonth(), resultado.fim());
    }

    // 4. Com período informado, usa exatamente o período pedido.
    @Test
    void deveUsarPeriodoInformadoQuandoFornecido() {

        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 1, 31);

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(List.of());

        AnalisarDespesasUseCase.Resultado resultado =
                analisarDespesasUseCase.executar(1L, inicio, fim);

        assertEquals(inicio, resultado.inicio());
        assertEquals(fim, resultado.fim());
    }

    // 5. Transferência enviada conta como despesa.
    @Test
    void deveContabilizarTransferenciaComoDespesa() {

        LocalDateTime agora = LocalDateTime.now();

        List<Transacao> transacoes = List.of(
                criarTransacao(TipoTransacao.TRANSFERENCIA, new BigDecimal("300"), "Transferência", StatusTransacao.APROVADA, agora)
        );

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(transacoes);

        AnalisarDespesasUseCase.Resultado resultado =
                analisarDespesasUseCase.executar(1L, null, null);

        assertEquals(new BigDecimal("300"), resultado.totalGeral());
    }
}

package com.apigestaofinanceira.transacoes.application;

import com.apigestaofinanceira.transacoes.application.usecases.GerarRelatorioCsvUseCase;
import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GerarRelatorioCsvUseCaseTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    private GerarRelatorioCsvUseCase gerarRelatorioCsvUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gerarRelatorioCsvUseCase = new GerarRelatorioCsvUseCase(transacaoRepository);
    }

    // 1. Deve conter o cabeçalho e uma linha por transação.
    @Test
    void deveGerarCsvComCabecalhoELinhasDeTransacoes() {

        Transacao transacao = new Transacao();
        transacao.setDataHora(LocalDateTime.of(2026, 1, 15, 10, 30));
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setCategoria("Alimentação");
        transacao.setDescricao("Mercado");
        transacao.setValor(new BigDecimal("150.50"));
        transacao.setStatus(StatusTransacao.APROVADA);
        transacao.setUsuarioId(1L);

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(List.of(transacao));

        byte[] csv = gerarRelatorioCsvUseCase.executar(1L, null, null);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertTrue(conteudo.contains("Data;Tipo;Categoria;Descricao;Valor (R$);Status;Motivo"));
        assertTrue(conteudo.contains("COMPRA"));
        assertTrue(conteudo.contains("Alimentação"));
        assertTrue(conteudo.contains("Mercado"));
        assertTrue(conteudo.contains("150,50"));
        assertTrue(conteudo.contains("APROVADA"));
    }

    // 2. O arquivo deve começar com o BOM UTF-8 (para o Excel reconhecer acentos).
    @Test
    void deveIniciarComBomUtf8() {

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(List.of());

        byte[] csv = gerarRelatorioCsvUseCase.executar(1L, null, null);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertEquals('\uFEFF', conteudo.charAt(0));
    }

    // 3. Transação rejeitada deve incluir o motivo na coluna correspondente.
    @Test
    void deveIncluirMotivoDeRejeicaoNoCsv() {

        Transacao transacao = new Transacao();
        transacao.setDataHora(LocalDateTime.of(2026, 1, 15, 10, 30));
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setValor(new BigDecimal("1000"));
        transacao.setStatus(StatusTransacao.REJEITADA);
        transacao.setMotivoRejeicao("Saldo insuficiente.");
        transacao.setUsuarioId(1L);

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(List.of(transacao));

        byte[] csv = gerarRelatorioCsvUseCase.executar(1L, null, null);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertTrue(conteudo.contains("REJEITADA"));
        assertTrue(conteudo.contains("Saldo insuficiente."));
    }

    // 4. Sem transações, deve gerar apenas o cabeçalho.
    @Test
    void deveGerarApenasCabecalhoQuandoNaoHaTransacoes() {

        when(transacaoRepository.buscarComFiltros(any(), any(), any(), any()))
                .thenReturn(List.of());

        byte[] csv = gerarRelatorioCsvUseCase.executar(1L, null, null);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        long linhas = conteudo.lines().count();

        assertEquals(1, linhas);
    }
}
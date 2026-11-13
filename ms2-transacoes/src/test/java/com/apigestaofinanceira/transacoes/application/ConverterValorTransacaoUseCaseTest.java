package com.apigestaofinanceira.transacoes.application;

import com.apigestaofinanceira.transacoes.application.usecases.ConverterValorTransacaoUseCase;
import com.apigestaofinanceira.transacoes.domain.ports.output.CambioClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ConverterValorTransacaoUseCaseTest {

    @Mock
    private CambioClientPort cambioClient;

    private ConverterValorTransacaoUseCase converterValorUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converterValorUseCase = new ConverterValorTransacaoUseCase(cambioClient);
    }

    // 1. Deve buscar cotações para as moedas suportadas (USD e EUR).
    @Test
    void deveBuscarCotacoesDeUsdEEur() {

        when(cambioClient.buscarCotacao("USD"))
                .thenReturn(Optional.of(
                        new CambioClientPort.Cotacao("USD", new BigDecimal("5.00"), LocalDate.now())));

        when(cambioClient.buscarCotacao("EUR"))
                .thenReturn(Optional.of(
                        new CambioClientPort.Cotacao("EUR", new BigDecimal("6.00"), LocalDate.now())));

        List<CambioClientPort.Cotacao> cotacoes = converterValorUseCase.buscarCotacoes();

        assertEquals(2, cotacoes.size());
    }

    // 2. Se a BrasilAPI falhar para uma moeda, a lista de cotações não quebra,
    //    apenas não traz aquela moeda.
    @Test
    void naoDeveQuebrarQuandoUmaMoedaNaoEstaDisponivel() {

        when(cambioClient.buscarCotacao("USD"))
                .thenReturn(Optional.of(
                        new CambioClientPort.Cotacao("USD", new BigDecimal("5.00"), LocalDate.now())));

        when(cambioClient.buscarCotacao("EUR"))
                .thenReturn(Optional.empty());

        List<CambioClientPort.Cotacao> cotacoes = converterValorUseCase.buscarCotacoes();

        assertEquals(1, cotacoes.size());
        assertEquals("USD", cotacoes.get(0).moeda());
    }

    // 3. Se a BrasilAPI falhar para todas as moedas, retorna lista vazia
    //    (quem consome decide como sinalizar indisponibilidade).
    @Test
    void deveRetornarListaVaziaQuandoNenhumaCotacaoEstaDisponivel() {

        when(cambioClient.buscarCotacao("USD")).thenReturn(Optional.empty());
        when(cambioClient.buscarCotacao("EUR")).thenReturn(Optional.empty());

        List<CambioClientPort.Cotacao> cotacoes = converterValorUseCase.buscarCotacoes();

        assertTrue(cotacoes.isEmpty());
    }

    // 4. Deve converter o valor em real pela taxa informada.
    @Test
    void deveConverterValorEmRealParaMoedaEstrangeira() {

        BigDecimal valorEmReal = new BigDecimal("100.00");
        BigDecimal taxa = new BigDecimal("5.00");

        BigDecimal resultado = converterValorUseCase.converter(valorEmReal, taxa);

        assertEquals(new BigDecimal("20.00"), resultado);
    }
}
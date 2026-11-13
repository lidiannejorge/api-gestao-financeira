package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.mapper;

import com.apigestaofinanceira.transacoes.application.usecases.ConverterValorTransacaoUseCase;
import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.CambioClientPort;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.ConversaoMoedaDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.CreateTransacaoRequestDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.TransacaoResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransacaoWebMapperTest {

    private CambioClientPort cambioClient;
    private ConverterValorTransacaoUseCase converterUseCase;
    private TransacaoWebMapper mapper;

    @BeforeEach
    void setUp() {
        cambioClient = mock(CambioClientPort.class);
        converterUseCase =
                new ConverterValorTransacaoUseCase(cambioClient);
        mapper = new TransacaoWebMapper(converterUseCase);
    }

    @Test
    void deveConverterDtoDeEntradaParaDominio() {
        CreateTransacaoRequestDTO dto =
                new CreateTransacaoRequestDTO();

        dto.setTipo(TipoTransacao.COMPRA);
        dto.setValor(new BigDecimal("150.90"));
        dto.setDescricao("Compra no supermercado");
        dto.setCategoria("ALIMENTACAO");
        dto.setUsuarioDestinoId(2L);

        Transacao resultado = mapper.toDomain(dto);

        assertEquals(TipoTransacao.COMPRA, resultado.getTipo());
        assertEquals(new BigDecimal("150.90"), resultado.getValor());
        assertEquals("Compra no supermercado", resultado.getDescricao());
        assertEquals("ALIMENTACAO", resultado.getCategoria());
        assertEquals(2L, resultado.getUsuarioDestinoId());

        assertNull(resultado.getId());
        assertNull(resultado.getUsuarioId());
    }

    @Test
    void deveMapearDadosDaTransacaoParaResponse() {
        Transacao transacao = criarTransacao();

        when(cambioClient.buscarCotacao("USD"))
                .thenReturn(Optional.empty());

        when(cambioClient.buscarCotacao("EUR"))
                .thenReturn(Optional.empty());

        TransacaoResponseDTO resultado =
                mapper.toResponse(transacao);

        assertEquals(1L, resultado.getId());
        assertEquals(TipoTransacao.COMPRA, resultado.getTipo());
        assertEquals(new BigDecimal("150.90"), resultado.getValor());
        assertEquals(transacao.getDataHora(), resultado.getDataHora());
        assertEquals("Compra no supermercado", resultado.getDescricao());
        assertEquals("ALIMENTACAO", resultado.getCategoria());
        assertEquals(1L, resultado.getUsuarioId());
        assertEquals(StatusTransacao.APROVADA, resultado.getStatus());

        assertEquals(1, resultado.getConversoes().size());
        assertFalse(resultado.getConversoes().get(0).isDisponivel());
    }

    @Test
    void deveConverterValorParaDolarEEuro() {
        LocalDate dataCotacao = LocalDate.of(2026, 10, 30);

        when(cambioClient.buscarCotacao("USD"))
                .thenReturn(Optional.of(
                        new CambioClientPort.Cotacao(
                                "USD",
                                new BigDecimal("5.4212"),
                                dataCotacao
                        )
                ));

        when(cambioClient.buscarCotacao("EUR"))
                .thenReturn(Optional.of(
                        new CambioClientPort.Cotacao(
                                "EUR",
                                new BigDecimal("6.0522"),
                                dataCotacao
                        )
                ));

        TransacaoResponseDTO resultado =
                mapper.toResponse(criarTransacao());

        assertEquals(2, resultado.getConversoes().size());

        ConversaoMoedaDTO dolar = resultado.getConversoes().get(0);

        assertEquals("USD", dolar.getMoeda());
        assertEquals(new BigDecimal("27.84"), dolar.getValorConvertido());
        assertEquals(new BigDecimal("5.4212"), dolar.getTaxa());
        assertEquals(dataCotacao, dolar.getDataTaxa());
        assertTrue(dolar.isDisponivel());

        ConversaoMoedaDTO euro = resultado.getConversoes().get(1);

        assertEquals("EUR", euro.getMoeda());
        assertEquals(new BigDecimal("24.93"), euro.getValorConvertido());
        assertEquals(new BigDecimal("6.0522"), euro.getTaxa());
        assertEquals(dataCotacao, euro.getDataTaxa());
        assertTrue(euro.isDisponivel());
    }

    @Test
    void deveIgnorarCotacaoComValorZero() {
        when(cambioClient.buscarCotacao("USD"))
                .thenReturn(Optional.of(
                        new CambioClientPort.Cotacao(
                                "USD",
                                BigDecimal.ZERO,
                                LocalDate.of(2026, 10, 30)
                        )
                ));

        when(cambioClient.buscarCotacao("EUR"))
                .thenReturn(Optional.empty());

        TransacaoResponseDTO resultado =
                mapper.toResponse(criarTransacao());

        assertEquals(1, resultado.getConversoes().size());
        assertFalse(resultado.getConversoes().get(0).isDisponivel());
        assertNull(resultado.getConversoes().get(0).getMoeda());
    }

    @Test
    void deveIgnorarCotacaoComValorNegativo() {
        when(cambioClient.buscarCotacao("USD"))
                .thenReturn(Optional.of(
                        new CambioClientPort.Cotacao(
                                "USD",
                                new BigDecimal("-5.00"),
                                LocalDate.of(2026, 10, 30)
                        )
                ));

        when(cambioClient.buscarCotacao("EUR"))
                .thenReturn(Optional.empty());

        TransacaoResponseDTO resultado =
                mapper.toResponse(criarTransacao());

        assertEquals(1, resultado.getConversoes().size());
        assertFalse(resultado.getConversoes().get(0).isDisponivel());
    }

    @Test
    void deveRetornarConversaoIndisponivelQuandoNaoHouverCotacoes() {
        when(cambioClient.buscarCotacao("USD"))
                .thenReturn(Optional.empty());

        when(cambioClient.buscarCotacao("EUR"))
                .thenReturn(Optional.empty());

        TransacaoResponseDTO resultado =
                mapper.toResponse(criarTransacao());

        assertEquals(1, resultado.getConversoes().size());

        ConversaoMoedaDTO conversao =
                resultado.getConversoes().get(0);

        assertNull(conversao.getMoeda());
        assertNull(conversao.getValorConvertido());
        assertNull(conversao.getTaxa());
        assertNull(conversao.getDataTaxa());
        assertFalse(conversao.isDisponivel());
    }

    @Test
    void deveContinuarQuandoApiNaoRetornarCotacaoDeUmaMoeda() {
        LocalDate dataCotacao = LocalDate.of(2026, 10, 30);

        when(cambioClient.buscarCotacao("USD"))
                .thenReturn(Optional.empty());

        when(cambioClient.buscarCotacao("EUR"))
                .thenReturn(Optional.of(
                        new CambioClientPort.Cotacao(
                                "EUR",
                                new BigDecimal("6.0522"),
                                dataCotacao
                        )
                ));

        TransacaoResponseDTO resultado =
                mapper.toResponse(criarTransacao());

        assertEquals(1, resultado.getConversoes().size());

        ConversaoMoedaDTO euro = resultado.getConversoes().get(0);

        assertEquals("EUR", euro.getMoeda());
        assertEquals(new BigDecimal("24.93"), euro.getValorConvertido());
        assertTrue(euro.isDisponivel());
    }

    private Transacao criarTransacao() {
        Transacao transacao = new Transacao();

        transacao.setId(1L);
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setValor(new BigDecimal("150.90"));
        transacao.setDataHora(
                LocalDateTime.of(2026, 10, 31, 10, 30)
        );
        transacao.setDescricao("Compra no supermercado");
        transacao.setCategoria("ALIMENTACAO");
        transacao.setUsuarioId(1L);
        transacao.setStatus(StatusTransacao.APROVADA);

        return transacao;
    }
}
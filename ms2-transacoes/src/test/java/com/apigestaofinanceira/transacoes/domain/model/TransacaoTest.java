package com.apigestaofinanceira.transacoes.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransacaoTest {

    @Test
    void deveCriarTransacaoComConstrutorCompleto() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 10, 31, 15, 30);

        Transacao transacao = new Transacao(
                10L,
                TipoTransacao.COMPRA,
                new BigDecimal("100.00"),
                dataHora,
                "Compra no mercado",
                "ALIMENTACAO",
                1L,
                null,
                StatusTransacao.APROVADA,
                null
        );

        assertEquals(10L, transacao.getId());
        assertEquals(TipoTransacao.COMPRA, transacao.getTipo());
        assertEquals(new BigDecimal("100.00"), transacao.getValor());
        assertEquals(dataHora, transacao.getDataHora());
        assertEquals("Compra no mercado", transacao.getDescricao());
        assertEquals("ALIMENTACAO", transacao.getCategoria());
        assertEquals(1L, transacao.getUsuarioId());
        assertNull(transacao.getUsuarioDestinoId());
        assertEquals(StatusTransacao.APROVADA, transacao.getStatus());
        assertNull(transacao.getMotivoRejeicao());
    }

    @Test
    void deveDefinirEConsultarId() {
        Transacao transacao = new Transacao();

        transacao.setId(20L);

        assertEquals(20L, transacao.getId());
    }

    @Test
    void deveDefinirUsuarioDestinoId() {
        Transacao transacao = new Transacao();

        transacao.setUsuarioDestinoId(5L);

        assertEquals(5L, transacao.getUsuarioDestinoId());
    }
}
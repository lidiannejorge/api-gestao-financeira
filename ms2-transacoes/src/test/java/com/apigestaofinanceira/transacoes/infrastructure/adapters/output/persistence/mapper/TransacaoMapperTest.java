package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.mapper;

import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.entity.TransacaoEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransacaoMapperTest {

    private final TransacaoMapper mapper = new TransacaoMapper();

    @Test
    void deveConverterTransacaoParaEntity() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 11, 1, 10, 30);

        Transacao transacao = new Transacao(
                1L,
                TipoTransacao.TRANSFERENCIA,
                new BigDecimal("250.00"),
                dataHora,
                "Transferência de teste",
                "Transferência",
                10L,
                20L,
                StatusTransacao.APROVADA,
                null
        );

        TransacaoEntity entity = mapper.toEntity(transacao);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals(TipoTransacao.TRANSFERENCIA, entity.getTipo());
        assertEquals(new BigDecimal("250.00"), entity.getValor());
        assertEquals(dataHora, entity.getDataHora());
        assertEquals("Transferência de teste", entity.getDescricao());
        assertEquals("Transferência", entity.getCategoria());
        assertEquals(10L, entity.getUsuarioId());
        assertEquals(20L, entity.getUsuarioDestinoId());
        assertEquals(StatusTransacao.APROVADA, entity.getStatus());
        assertNull(entity.getMotivoRejeicao());
    }

    @Test
    void deveConverterEntityParaTransacao() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 11, 1, 11, 45);

        TransacaoEntity entity = new TransacaoEntity();

        entity.setId(2L);
        entity.setTipo(TipoTransacao.COMPRA);
        entity.setValor(new BigDecimal("89.90"));
        entity.setDataHora(dataHora);
        entity.setDescricao("Compra de teste");
        entity.setCategoria("Alimentação");
        entity.setUsuarioId(15L);
        entity.setUsuarioDestinoId(null);
        entity.setStatus(StatusTransacao.REJEITADA);
        entity.setMotivoRejeicao("Limite diário excedido");

        Transacao transacao = mapper.toDomain(entity);

        assertNotNull(transacao);
        assertEquals(2L, transacao.getId());
        assertEquals(TipoTransacao.COMPRA, transacao.getTipo());
        assertEquals(new BigDecimal("89.90"), transacao.getValor());
        assertEquals(dataHora, transacao.getDataHora());
        assertEquals("Compra de teste", transacao.getDescricao());
        assertEquals("Alimentação", transacao.getCategoria());
        assertEquals(15L, transacao.getUsuarioId());
        assertNull(transacao.getUsuarioDestinoId());
        assertEquals(StatusTransacao.REJEITADA, transacao.getStatus());
        assertEquals("Limite diário excedido", transacao.getMotivoRejeicao());
    }
}
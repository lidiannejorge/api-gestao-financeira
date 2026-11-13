package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence;

import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.entity.TransacaoEntity;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.mapper.TransacaoMapper;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.repository.SpringDataTransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoPersistenceAdapterTest {

    @Mock
    private SpringDataTransacaoRepository repository;

    private TransacaoPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        TransacaoMapper mapper = new TransacaoMapper();
        adapter = new TransacaoPersistenceAdapter(repository, mapper);
    }

    @Test
    void deveSalvarTransacao() {
        Transacao transacao = criarTransacao(1L, 10L);

        when(repository.save(any(TransacaoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transacao resultado = adapter.salvar(transacao);

        ArgumentCaptor<TransacaoEntity> captor =
                ArgumentCaptor.forClass(TransacaoEntity.class);

        verify(repository).save(captor.capture());

        TransacaoEntity entitySalva = captor.getValue();

        assertEquals(1L, entitySalva.getId());
        assertEquals(TipoTransacao.DEPOSITO, entitySalva.getTipo());
        assertEquals(new BigDecimal("150.00"), entitySalva.getValor());
        assertEquals(10L, entitySalva.getUsuarioId());

        assertNotNull(resultado);
        assertEquals(transacao.getId(), resultado.getId());
        assertEquals(transacao.getTipo(), resultado.getTipo());
        assertEquals(transacao.getValor(), resultado.getValor());
        assertEquals(transacao.getUsuarioId(), resultado.getUsuarioId());
    }

    @Test
    void deveBuscarTransacaoPorId() {
        TransacaoEntity entity = criarEntity(1L, 10L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        Optional<Transacao> resultado = adapter.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals(TipoTransacao.DEPOSITO, resultado.get().getTipo());
        assertEquals(new BigDecimal("150.00"), resultado.get().getValor());

        verify(repository).findById(1L);
    }

    @Test
    void deveRetornarVazioQuandoTransacaoNaoExistir() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<Transacao> resultado = adapter.buscarPorId(999L);

        assertTrue(resultado.isEmpty());

        verify(repository).findById(999L);
    }

    @Test
    void deveBuscarTransacoesPorUsuarioId() {
        List<TransacaoEntity> entities = List.of(
                criarEntity(1L, 10L),
                criarEntity(2L, 10L)
        );

        when(repository.findByUsuarioId(10L))
                .thenReturn(entities);

        List<Transacao> resultado = adapter.buscarPorUsuarioId(10L);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());

        verify(repository).findByUsuarioId(10L);
    }

    @Test
    void deveBuscarTransacoesComFiltros() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 30, 23, 59);

        List<TransacaoEntity> entities = List.of(
                criarEntity(1L, 10L)
        );

        when(repository.buscarComFiltros(
                10L, inicio, fim, TipoTransacao.DEPOSITO
        )).thenReturn(entities);

        List<Transacao> resultado = adapter.buscarComFiltros(
                10L, inicio, fim, TipoTransacao.DEPOSITO
        );

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(TipoTransacao.DEPOSITO, resultado.get(0).getTipo());

        verify(repository).buscarComFiltros(
                10L, inicio, fim, TipoTransacao.DEPOSITO
        );
    }

    private Transacao criarTransacao(Long id, Long usuarioId) {
        return new Transacao(
                id,
                TipoTransacao.DEPOSITO,
                new BigDecimal("150.00"),
                LocalDateTime.of(2026, 11, 1, 10, 30),
                "Depósito de teste",
                "Salário",
                usuarioId,
                null,
                StatusTransacao.APROVADA,
                null
        );
    }

    private TransacaoEntity criarEntity(Long id, Long usuarioId) {
        TransacaoEntity entity = new TransacaoEntity();

        entity.setId(id);
        entity.setTipo(TipoTransacao.DEPOSITO);
        entity.setValor(new BigDecimal("150.00"));
        entity.setDataHora(LocalDateTime.of(2026, 11, 1, 10, 30));
        entity.setDescricao("Depósito de teste");
        entity.setCategoria("Salário");
        entity.setUsuarioId(usuarioId);
        entity.setUsuarioDestinoId(null);
        entity.setStatus(StatusTransacao.APROVADA);
        entity.setMotivoRejeicao(null);

        return entity;
    }
}
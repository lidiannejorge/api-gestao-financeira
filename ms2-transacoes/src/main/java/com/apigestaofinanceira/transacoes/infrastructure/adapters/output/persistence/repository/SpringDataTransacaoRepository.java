package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.repository;

import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.entity.TransacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataTransacaoRepository
        extends JpaRepository<TransacaoEntity, Long> {

    // Busca todas as transações de um usuário
    List<TransacaoEntity> findByUsuarioId(Long usuarioId);

    // Busca transações com filtros opcionais
    @Query("""
        SELECT t FROM TransacaoEntity t
        WHERE t.usuarioId = :usuarioId
        AND t.dataHora >= COALESCE(:inicio, t.dataHora)
        AND t.dataHora <= COALESCE(:fim, t.dataHora)
        AND t.tipo = COALESCE(:tipo, t.tipo)
    """)
    List<TransacaoEntity> buscarComFiltros(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("tipo") TipoTransacao tipo
    );
}
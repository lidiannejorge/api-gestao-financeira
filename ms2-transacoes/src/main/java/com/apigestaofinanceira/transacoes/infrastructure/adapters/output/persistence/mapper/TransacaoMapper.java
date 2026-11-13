package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.mapper;

import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.entity.TransacaoEntity;
import org.springframework.stereotype.Component;

@Component
public class TransacaoMapper {

    public TransacaoEntity toEntity(Transacao transacao) {
        TransacaoEntity entity = new TransacaoEntity();

        entity.setId(transacao.getId());
        entity.setTipo(transacao.getTipo());
        entity.setValor(transacao.getValor());
        entity.setDataHora(transacao.getDataHora());
        entity.setDescricao(transacao.getDescricao());
        entity.setCategoria(transacao.getCategoria());
        entity.setUsuarioId(transacao.getUsuarioId());
        entity.setUsuarioDestinoId(transacao.getUsuarioDestinoId());
        entity.setStatus(transacao.getStatus());
        entity.setMotivoRejeicao(transacao.getMotivoRejeicao());

        return entity;
    }

    public Transacao toDomain(TransacaoEntity entity) {
        Transacao transacao = new Transacao();

        transacao.setId(entity.getId());
        transacao.setTipo(entity.getTipo());
        transacao.setValor(entity.getValor());
        transacao.setDataHora(entity.getDataHora());
        transacao.setDescricao(entity.getDescricao());
        transacao.setCategoria(entity.getCategoria());
        transacao.setUsuarioId(entity.getUsuarioId());
        transacao.setUsuarioDestinoId(entity.getUsuarioDestinoId());
        transacao.setStatus(entity.getStatus());
        transacao.setMotivoRejeicao(entity.getMotivoRejeicao());

        return transacao;
    }
}

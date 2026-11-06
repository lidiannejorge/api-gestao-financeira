package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.mapper;

import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.CreateTransacaoRequestDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.TransacaoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class TransacaoWebMapper {

    public Transacao toDomain(CreateTransacaoRequestDTO dto) {
        Transacao transacao = new Transacao();

        transacao.setTipo(dto.getTipo());
        transacao.setValor(dto.getValor());
        transacao.setDescricao(dto.getDescricao());
        transacao.setCategoria(dto.getCategoria());
        transacao.setUsuarioDestinoId(dto.getUsuarioDestinoId());

        return transacao;
    }

    public TransacaoResponseDTO toResponse(Transacao transacao) {
        TransacaoResponseDTO dto = new TransacaoResponseDTO();

        dto.setId(transacao.getId());
        dto.setTipo(transacao.getTipo());
        dto.setValor(transacao.getValor());
        dto.setDataHora(transacao.getDataHora());
        dto.setDescricao(transacao.getDescricao());
        dto.setCategoria(transacao.getCategoria());
        dto.setUsuarioId(transacao.getUsuarioId());
        dto.setUsuarioDestinoId(transacao.getUsuarioDestinoId());
        dto.setStatus(transacao.getStatus());
        dto.setMotivoRejeicao(transacao.getMotivoRejeicao());

        return dto;
    }
}

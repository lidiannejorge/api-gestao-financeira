package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.mapper;


import com.apigestaofinanceira.transacoes.application.usecases.ConverterValorTransacaoUseCase;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.CambioClientPort;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.ConversaoMoedaDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.CreateTransacaoRequestDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.TransacaoResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TransacaoWebMapper {

    private final ConverterValorTransacaoUseCase converterValorUseCase;

    public TransacaoWebMapper(
            ConverterValorTransacaoUseCase converterValorUseCase) {
        this.converterValorUseCase = converterValorUseCase;
    }

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
        dto.setConversoes(montarConversoes(transacao));

        return dto;
    }

    private List<ConversaoMoedaDTO> montarConversoes(
            Transacao transacao) {

        List<CambioClientPort.Cotacao> cotacoes =
                converterValorUseCase.buscarCotacoes();

        List<CambioClientPort.Cotacao> cotacoesValidas =
                cotacoes.stream()
                        .filter(cotacao ->
                                cotacao.valor() != null
                                        && cotacao.valor().compareTo(
                                        BigDecimal.ZERO) > 0)
                        .toList();

        if (cotacoesValidas.isEmpty()) {
            return List.of(
                    new ConversaoMoedaDTO(
                            null, null, null, null, false)
            );
        }

        return cotacoesValidas.stream()
                .map(cotacao -> new ConversaoMoedaDTO(
                        cotacao.moeda(),
                        converterValorUseCase.converter(
                                transacao.getValor(),
                                cotacao.valor()),
                        cotacao.valor(),
                        cotacao.data(),
                        true))
                .toList();
    }
}
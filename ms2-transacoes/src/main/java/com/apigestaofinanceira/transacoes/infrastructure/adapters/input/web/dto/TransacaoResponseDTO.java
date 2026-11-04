package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto;

import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransacaoResponseDTO {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "COMPRA")
    private TipoTransacao tipo;

    @Schema(example = "150.90")
    private BigDecimal valor;

    @Schema(example = "2026-01-15T10:30:00")
    private LocalDateTime dataHora;

    @Schema(example = "Compra no supermercado")
    private String descricao;

    @Schema(example = "Alimentação")
    private String categoria;

    @Schema(example = "1")
    private Long usuarioId;

    @Schema(example = "null")
    private Long usuarioDestinoId;

    @Schema(example = "APROVADA")
    private StatusTransacao status;

    @Schema(example = "null")
    private String motivoRejeicao;

    public TransacaoResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getUsuarioDestinoId() {
        return usuarioDestinoId;
    }

    public void setUsuarioDestinoId(Long usuarioDestinoId) {
        this.usuarioDestinoId = usuarioDestinoId;
    }

    public StatusTransacao getStatus() {
        return status;
    }

    public void setStatus(StatusTransacao status) {
        this.status = status;
    }

    public String getMotivoRejeicao() {
        return motivoRejeicao;
    }

    public void setMotivoRejeicao(String motivoRejeicao) {
        this.motivoRejeicao = motivoRejeicao;
    }
}

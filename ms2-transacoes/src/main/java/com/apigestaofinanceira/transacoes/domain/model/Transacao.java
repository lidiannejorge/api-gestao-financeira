package com.apigestaofinanceira.transacoes.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transacao {

    private Long id;
    private TipoTransacao tipo;
    private BigDecimal valor;
    private LocalDateTime dataHora;
    private String descricao;
    private String categoria;
    private Long usuarioId;
    private Long usuarioDestinoId;
    private StatusTransacao status;
    private String motivoRejeicao;

    // Construtor vazio
    public Transacao() {
    }

    // Construtor completo
    public Transacao(
            Long id,
            TipoTransacao tipo,
            BigDecimal valor,
            LocalDateTime dataHora,
            String descricao,
            String categoria,
            Long usuarioId,
            Long usuarioDestinoId,
            StatusTransacao status,
            String motivoRejeicao) {

        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
        this.descricao = descricao;
        this.categoria = categoria;
        this.usuarioId = usuarioId;
        this.usuarioDestinoId = usuarioDestinoId;
        this.status = status;
        this.motivoRejeicao = motivoRejeicao;
    }

    // Getters e Setters

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
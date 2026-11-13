package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto;

import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateTransacaoRequestDTO {

    @Schema(example = "COMPRA")
    @NotNull(message = "O tipo da transação é obrigatório.")
    private TipoTransacao tipo;

    @Schema(example = "150.90")
    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal valor;

    @Schema(example = "Compra no supermercado")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
    private String descricao;

    @Schema(example = "Alimentação")
    @Size(max = 100, message = "A categoria deve ter no máximo 100 caracteres.")
    private String categoria;

    @Schema(example = "null", description = "Obrigatório apenas para transferências")
    private Long usuarioDestinoId;

    public CreateTransacaoRequestDTO() {
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

    public Long getUsuarioDestinoId() {
        return usuarioDestinoId;
    }

    public void setUsuarioDestinoId(Long usuarioDestinoId) {
        this.usuarioDestinoId = usuarioDestinoId;
    }
}
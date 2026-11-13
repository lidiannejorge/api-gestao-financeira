package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ConversaoMoedaDTO {

    private String moeda;
    private BigDecimal valorConvertido;
    private BigDecimal taxa;
    private LocalDate dataTaxa;
    private boolean disponivel;

    public ConversaoMoedaDTO() {
    }

    public ConversaoMoedaDTO(
            String moeda,
            BigDecimal valorConvertido,
            BigDecimal taxa,
            LocalDate dataTaxa,
            boolean disponivel) {

        this.moeda = moeda;
        this.valorConvertido = valorConvertido;
        this.taxa = taxa;
        this.dataTaxa = dataTaxa;
        this.disponivel = disponivel;
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    public BigDecimal getValorConvertido() {
        return valorConvertido;
    }

    public void setValorConvertido(BigDecimal valorConvertido) {
        this.valorConvertido = valorConvertido;
    }

    public BigDecimal getTaxa() {
        return taxa;
    }

    public void setTaxa(BigDecimal taxa) {
        this.taxa = taxa;
    }

    public LocalDate getDataTaxa() {
        return dataTaxa;
    }

    public void setDataTaxa(LocalDate dataTaxa) {
        this.dataTaxa = dataTaxa;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }
}
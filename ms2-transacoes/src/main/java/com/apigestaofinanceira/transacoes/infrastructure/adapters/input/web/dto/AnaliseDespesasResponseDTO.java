package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnaliseDespesasResponseDTO {

    private LocalDate inicio;
    private LocalDate fim;
    private BigDecimal totalGeral;
    private List<ResumoCategoria> porCategoria;
    private Map<LocalDate, BigDecimal> porDia;
    private Map<String, BigDecimal> porMes;

    public AnaliseDespesasResponseDTO() {
    }

    public AnaliseDespesasResponseDTO(
            LocalDate inicio,
            LocalDate fim,
            BigDecimal totalGeral,
            List<ResumoCategoria> porCategoria,
            Map<LocalDate, BigDecimal> porDia,
            Map<String, BigDecimal> porMes) {

        this.inicio = inicio;
        this.fim = fim;
        this.totalGeral = totalGeral;
        this.porCategoria = porCategoria;
        this.porDia = porDia;
        this.porMes = porMes;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }

    public BigDecimal getTotalGeral() {
        return totalGeral;
    }

    public void setTotalGeral(BigDecimal totalGeral) {
        this.totalGeral = totalGeral;
    }

    public List<ResumoCategoria> getPorCategoria() {
        return porCategoria;
    }

    public void setPorCategoria(List<ResumoCategoria> porCategoria) {
        this.porCategoria = porCategoria;
    }

    public Map<LocalDate, BigDecimal> getPorDia() {
        return porDia;
    }

    public void setPorDia(Map<LocalDate, BigDecimal> porDia) {
        this.porDia = porDia;
    }

    public Map<String, BigDecimal> getPorMes() {
        return porMes;
    }

    public void setPorMes(Map<String, BigDecimal> porMes) {
        this.porMes = porMes;
    }

    public static class ResumoCategoria {

        private String categoria;
        private BigDecimal total;

        public ResumoCategoria() {
        }

        public ResumoCategoria(String categoria, BigDecimal total) {
            this.categoria = categoria;
            this.total = total;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }
    }
}
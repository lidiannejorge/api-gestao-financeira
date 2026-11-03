package com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto;

import java.util.List;

public class ImportacaoUsuarioResponseDTO {

    private int totalSucesso;
    private int totalFalhas;
    private List<String> erros;

    public ImportacaoUsuarioResponseDTO(
            int totalSucesso,
            int totalFalhas,
            List<String> erros) {
        this.totalSucesso = totalSucesso;
        this.totalFalhas = totalFalhas;
        this.erros = erros;
    }

    public int getTotalSucesso() {
        return totalSucesso;
    }

    public int getTotalFalhas() {
        return totalFalhas;
    }

    public List<String> getErros() {
        return erros;
    }
}
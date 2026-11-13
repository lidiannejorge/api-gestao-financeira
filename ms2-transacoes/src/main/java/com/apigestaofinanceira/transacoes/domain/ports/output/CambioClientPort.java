package com.apigestaofinanceira.transacoes.domain.ports.output;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface CambioClientPort {

    Optional<Cotacao> buscarCotacao(String moeda);

    record Cotacao(
            String moeda,
            BigDecimal valor,
            LocalDate data
    ) {
    }
}
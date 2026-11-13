package com.apigestaofinanceira.transacoes.application.usecases;

import com.apigestaofinanceira.transacoes.domain.ports.output.CambioClientPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConverterValorTransacaoUseCase {

    // Moedas que o PO pediu para mostrar em toda consulta de transação.
    private static final List<String> MOEDAS_SUPORTADAS = List.of("USD", "EUR");

    private final CambioClientPort cambioClient;

    public ConverterValorTransacaoUseCase(CambioClientPort cambioClient) {
        this.cambioClient = cambioClient;
    }

    public List<CambioClientPort.Cotacao> buscarCotacoes() {

        List<CambioClientPort.Cotacao> cotacoes = new ArrayList<>();

        for (String moeda : MOEDAS_SUPORTADAS) {
            cambioClient.buscarCotacao(moeda)
                    .ifPresent(cotacoes::add);
        }

        return cotacoes;
    }

    public BigDecimal converter(BigDecimal valorEmReal, BigDecimal taxa) {
        return valorEmReal.divide(taxa, 2, RoundingMode.HALF_UP);
    }
}
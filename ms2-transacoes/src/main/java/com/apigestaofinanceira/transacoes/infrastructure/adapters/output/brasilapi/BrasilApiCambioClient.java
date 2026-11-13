package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.brasilapi;

import com.apigestaofinanceira.transacoes.domain.ports.output.CambioClientPort;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class BrasilApiCambioClient implements CambioClientPort {

    private final RestClient brasilApiRestClient;
    private final Clock clock;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BrasilApiCambioClient(
            @Qualifier("brasilApiRestClient") RestClient brasilApiRestClient,
            Clock clock) {
        this.brasilApiRestClient = brasilApiRestClient;
        this.clock = clock;
    }

    @Override
    public Optional<Cotacao> buscarCotacao(String moeda) {

        // A BrasilAPI não permite consultar a cotação do dia de hoje
        // (retorna 400 - NO_TODAY_DATE), então a busca sempre começa
        // a partir de ontem. A própria API já devolve a última cotação
        // disponível em fins de semana/feriados (ex.: consultar sábado
        // retorna a de sexta-feira).
        for (int dias = 1; dias <= 7; dias++) {

            LocalDate dataConsulta = LocalDate.now(clock).minusDays(dias);

            Optional<Cotacao> cotacao =
                    buscarCotacaoNaData(moeda, dataConsulta);

            if (cotacao.isPresent()) {
                return cotacao;
            }
        }

        return Optional.empty();
    }

    private Optional<Cotacao> buscarCotacaoNaData(
            String moeda, LocalDate data) {

        try {

            BrasilApiResponse resposta = brasilApiRestClient.get()
                    .uri(
                            "/api/cambio/v1/cotacao/{moeda}/{data}",
                            moeda,
                            data.format(FORMATO_DATA)
                    )
                    .retrieve()
                    .body(BrasilApiResponse.class);

            if (resposta == null
                    || resposta.cotacoes() == null
                    || resposta.cotacoes().isEmpty()) {
                return Optional.empty();
            }

            CotacaoItem item =
                    resposta.cotacoes().get(resposta.cotacoes().size() - 1);

            if (item == null
                    || item.cotacaoVenda() == null
                    || item.cotacaoVenda().compareTo(BigDecimal.ZERO) <= 0) {
                return Optional.empty();
            }

            LocalDate dataCotacao = resposta.data() != null
                    ? LocalDate.parse(resposta.data())
                    : data;

            return Optional.of(
                    new Cotacao(
                            moeda,
                            item.cotacaoVenda(),
                            dataCotacao
                    )
            );

        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    private record BrasilApiResponse(
            String moeda,
            String data,
            List<CotacaoItem> cotacoes
    ) {}

    private record CotacaoItem(

            @JsonProperty("cotacao_compra")
            BigDecimal cotacaoCompra,

            @JsonProperty("cotacao_venda")
            BigDecimal cotacaoVenda

    ) {}
}

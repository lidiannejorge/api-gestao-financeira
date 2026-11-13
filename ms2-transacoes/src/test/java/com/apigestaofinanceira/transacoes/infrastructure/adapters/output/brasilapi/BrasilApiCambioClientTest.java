package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.brasilapi;

import com.apigestaofinanceira.transacoes.domain.ports.output.CambioClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrasilApiCambioClientTest {

    private MockRestServiceServer servidor;
    private BrasilApiCambioClient client;

    // Data fixa usada como "hoje" nos testes, para que sejam
    // determinísticos. A busca na BrasilAPI sempre começa em "ontem"
    // (hoje - 1), pois a API não permite consultar a data de hoje.
    private final LocalDate hoje = LocalDate.of(2026, 11, 10);
    private final Clock relogioFixo = Clock.fixed(
            hoje.atStartOfDay(ZoneOffset.UTC).toInstant(),
            ZoneOffset.UTC);

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost");

        servidor = MockRestServiceServer.bindTo(builder).build();

        client = new BrasilApiCambioClient(builder.build(), relogioFixo);
    }

    @Test
    void deveRetornarCotacaoDeOntemQuandoApiResponderComSucesso() {
        LocalDate ontem = hoje.minusDays(1);

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + ontem.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "moeda": "USD",
                            "data": "%s",
                            "cotacoes": [
                                {"cotacao_compra": 5.40, "cotacao_venda": 5.50},
                                {"cotacao_compra": 5.41, "cotacao_venda": 5.51}
                            ]
                        }
                        """.formatted(ontem), MediaType.APPLICATION_JSON));

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isPresent());
        assertEquals("USD", resultado.get().moeda());
        assertEquals(new BigDecimal("5.51"), resultado.get().valor());
        assertEquals(ontem, resultado.get().data());

        servidor.verify();
    }

    @Test
    void deveBuscarDoisDiasAtrasQuandoOntemNaoPossuirCotacao() {
        LocalDate ontem = hoje.minusDays(1);
        LocalDate anteontem = hoje.minusDays(2);

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/EUR/"
                                + ontem.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withResourceNotFound());

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/EUR/"
                                + anteontem.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "moeda": "EUR",
                            "data": "%s",
                            "cotacoes": [
                                {"cotacao_compra": 6.00, "cotacao_venda": 6.10}
                            ]
                        }
                        """.formatted(anteontem), MediaType.APPLICATION_JSON));

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("EUR");

        assertTrue(resultado.isPresent());
        assertEquals(new BigDecimal("6.10"), resultado.get().valor());
        assertEquals(anteontem, resultado.get().data());

        servidor.verify();
    }

    // Simula o cenário reportado pelo PO: a busca já começa em ontem,
    // então nenhuma requisição deve ser feita para a data de hoje.
    @Test
    void naoDeveConsultarACotacaoDoDiaAtual() {
        LocalDate ontem = hoje.minusDays(1);

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + ontem.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "moeda": "USD",
                            "data": "%s",
                            "cotacoes": [
                                {"cotacao_compra": 5.00, "cotacao_venda": 5.10}
                            ]
                        }
                        """.formatted(ontem), MediaType.APPLICATION_JSON));

        client.buscarCotacao("USD");

        // Se alguma requisição tivesse sido feita para a data de hoje,
        // o MockRestServiceServer teria falhado por requisição
        // inesperada - chegar até aqui já comprova que isso não ocorreu.
        servidor.verify();
    }

    @Test
    void deveRetornarVazioQuandoNaoEncontrarCotacaoNosUltimosSeteDias() {

        for (int dias = 1; dias <= 7; dias++) {
            LocalDate data = hoje.minusDays(dias);

            servidor.expect(requestTo(
                            "http://localhost/api/cambio/v1/cotacao/USD/"
                                    + data.format(FORMATO)))
                    .andExpect(method(GET))
                    .andRespond(withResourceNotFound());
        }

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isEmpty());

        servidor.verify();
    }

    @Test
    void deveIgnorarCotacaoComValorZero() {
        LocalDate ontem = hoje.minusDays(1);

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + ontem.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "data": "%s",
                            "cotacoes": [
                                {"cotacao_compra": 0, "cotacao_venda": 0}
                            ]
                        }
                        """.formatted(ontem), MediaType.APPLICATION_JSON));

        for (int dias = 2; dias <= 7; dias++) {
            LocalDate data = hoje.minusDays(dias);

            servidor.expect(requestTo(
                            "http://localhost/api/cambio/v1/cotacao/USD/"
                                    + data.format(FORMATO)))
                    .andExpect(method(GET))
                    .andRespond(withResourceNotFound());
        }

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isEmpty());

        servidor.verify();
    }

    @Test
    void deveIgnorarCotacaoComValorNegativo() {
        LocalDate ontem = hoje.minusDays(1);

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + ontem.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "data": "%s",
                            "cotacoes": [
                                {"cotacao_compra": 5.00, "cotacao_venda": -1.00}
                            ]
                        }
                        """.formatted(ontem), MediaType.APPLICATION_JSON));

        for (int dias = 2; dias <= 7; dias++) {
            LocalDate data = hoje.minusDays(dias);

            servidor.expect(requestTo(
                            "http://localhost/api/cambio/v1/cotacao/USD/"
                                    + data.format(FORMATO)))
                    .andExpect(method(GET))
                    .andRespond(withResourceNotFound());
        }

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isEmpty());

        servidor.verify();
    }

    @Test
    void deveUsarDataConsultadaQuandoRespostaNaoInformarData() {
        LocalDate ontem = hoje.minusDays(1);

        servidor.expect(requestTo(
                        "http://localhost/api/cambio/v1/cotacao/USD/"
                                + ontem.format(FORMATO)))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                            "moeda": "USD",
                            "cotacoes": [
                                {"cotacao_compra": 5.00, "cotacao_venda": 5.25}
                            ]
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<CambioClientPort.Cotacao> resultado =
                client.buscarCotacao("USD");

        assertTrue(resultado.isPresent());
        assertEquals(ontem, resultado.get().data());
        assertEquals(new BigDecimal("5.25"), resultado.get().valor());

        servidor.verify();
    }
}

package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.mockapi;

import com.apigestaofinanceira.transacoes.domain.ports.output.ContaClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MockApiContaClientTest {

    private MockRestServiceServer servidor;
    private MockApiContaClient client;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://mockapi-teste.com");

        servidor = MockRestServiceServer.bindTo(builder).build();

        client = new MockApiContaClient(builder.build());
    }

    @Test
    void deveConsultarContaComSucesso() {

        servidor.expect(requestTo(
                        "http://mockapi-teste.com/contas?usuarioId=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{
                            "id": "10",
                            "usuarioId": 1,
                            "saldo": 500.00,
                            "limiteDiario": 1000.00,
                            "limitePorTransacao": 500.00
                        }]
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        ContaClientPort.ContaDados conta = client.consultarConta(1L);

        assertEquals("10", conta.id());
        assertEquals(1L, conta.usuarioId());
        assertEquals(new BigDecimal("500.00"), conta.saldo());
        assertEquals(new BigDecimal("1000.00"), conta.limiteDiario());
        assertEquals(new BigDecimal("500.00"), conta.limitePorTransacao());

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoContaNaoForEncontrada() {

        servidor.expect(requestTo(
                        "http://mockapi-teste.com/contas?usuarioId=1"))
                .andRespond(withSuccess("[]",
                        org.springframework.http.MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalArgumentException.class,
                () -> client.consultarConta(1L)
        );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoMockApiRetornarErroHttp() {

        servidor.expect(requestTo(
                        "http://mockapi-teste.com/contas?usuarioId=1"))
                .andRespond(withServerError());

        assertThrows(
                org.springframework.web.client.HttpServerErrorException.class,
                () -> client.consultarConta(1L)
        );

        servidor.verify();
    }

    @Test
    void deveAtualizarSaldoDaConta() {

        servidor.expect(requestTo(
                        "http://mockapi-teste.com/contas?usuarioId=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{
                            "id": "10",
                            "usuarioId": 1,
                            "saldo": 500.00,
                            "limiteDiario": 1000.00,
                            "limitePorTransacao": 500.00
                        }]
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        servidor.expect(requestTo(
                        "http://mockapi-teste.com/contas/10"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        assertDoesNotThrow(
                () -> client.atualizarSaldo(1L, new BigDecimal("700.00"))
        );

        servidor.verify();
    }
}
package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.ms1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class Ms1UsuarioClientTest {

    private MockRestServiceServer server;
    private Ms1UsuarioClient usuarioClient;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost");

        server = MockRestServiceServer.bindTo(builder).build();

        usuarioClient = new Ms1UsuarioClient(builder.build());
    }

    @Test
    void deveRetornarTrueQuandoUsuarioExistir() {
        server.expect(requestTo("http://localhost/api/usuarios/10/existe"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        boolean resultado = usuarioClient.usuarioExiste(10L);

        assertTrue(resultado);
        server.verify();
    }

    @Test
    void deveRetornarFalseQuandoUsuarioNaoExistir() {
        server.expect(requestTo("http://localhost/api/usuarios/10/existe"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        boolean resultado = usuarioClient.usuarioExiste(10L);

        assertFalse(resultado);
        server.verify();
    }

    @Test
    void devePropagarExcecaoQuandoMs1RetornarErroInterno() {
        server.expect(requestTo("http://localhost/api/usuarios/10/existe"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(
                org.springframework.web.client.RestClientResponseException.class,
                () -> usuarioClient.usuarioExiste(10L)
        );

        server.verify();
    }
}

package com.apigestaofinanceira.transacoes.infrastructure.config.security;

import com.apigestaofinanceira.transacoes.infrastructure.config.security.Ms1AuthenticationProvider;
import com.apigestaofinanceira.transacoes.infrastructure.config.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class Ms1AuthenticationProviderTest {

    private Ms1AuthenticationProvider provider;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .baseUrl("http://localhost")
                .build();

        provider = new Ms1AuthenticationProvider(restClient);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {

        server.expect(requestTo(
                        "http://localhost/api/usuarios/autenticar"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                            "email": "teste@email.com",
                            "password": "123456"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                            "id": 10,
                            "name": "Teste",
                            "email": "teste@email.com"
                        }
                        """, MediaType.APPLICATION_JSON));

        Authentication tentativa =
                new UsernamePasswordAuthenticationToken(
                        "teste@email.com",
                        "123456"
                );

        Authentication resultado = provider.authenticate(tentativa);

        assertNotNull(resultado);
        assertTrue(resultado.isAuthenticated());
        assertNull(resultado.getCredentials());

        UsuarioAutenticado usuario =
                (UsuarioAutenticado) resultado.getPrincipal();

        assertEquals(10L, usuario.getId());
        assertEquals("teste@email.com", usuario.getEmail());

        assertTrue(resultado.getAuthorities().stream()
                .anyMatch(autoridade ->
                        autoridade.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void deveRejeitarCredenciaisInvalidas() {

        server.expect(requestTo(
                        "http://localhost/api/usuarios/autenticar"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        Authentication tentativa =
                new UsernamePasswordAuthenticationToken(
                        "teste@email.com",
                        "senha-errada"
                );

        assertThrows(
                BadCredentialsException.class,
                () -> provider.authenticate(tentativa)
        );
    }

    @Test
    void deveRejeitarRespostaSemUsuario() {

        server.expect(requestTo(
                        "http://localhost/api/usuarios/autenticar"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        Authentication tentativa =
                new UsernamePasswordAuthenticationToken(
                        "teste@email.com",
                        "123456"
                );

        assertThrows(
                BadCredentialsException.class,
                () -> provider.authenticate(tentativa)
        );
    }

    @Test
    void deveAceitarUsernamePasswordAuthenticationToken() {

        assertTrue(provider.supports(
                UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void naoDeveAceitarOutroTipoDeAuthentication() {

        assertFalse(provider.supports(Authentication.class));
    }
}
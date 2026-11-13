package com.apigestaofinanceira.transacoes.infrastructure.config.security;

import com.apigestaofinanceira.transacoes.infrastructure.config.security.TitularidadeFilter;
import com.apigestaofinanceira.transacoes.infrastructure.config.security.UsuarioAutenticado;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TitularidadeFilterTest {

    private TitularidadeFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TitularidadeFilter();

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal())
                .thenReturn(new UsuarioAutenticado(10L, "teste@email.com"));

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRetornar400QuandoCabecalhoNaoForInformado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(400, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void deveRetornar400QuandoIdForInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Usuario-Id", "abc");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(400, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void deveRetornar403QuandoUsuarioTentarAcessarDadosDeOutro() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Usuario-Id", "20");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void devePermitirAcessoQuandoIdPertencerAoUsuarioAutenticado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Usuario-Id", "10");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(request, response);
    }
}
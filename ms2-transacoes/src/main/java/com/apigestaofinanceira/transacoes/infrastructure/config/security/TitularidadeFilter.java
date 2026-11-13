package com.apigestaofinanceira.transacoes.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TitularidadeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal()
                instanceof UsuarioAutenticado usuarioAutenticado) {

            String usuarioIdHeader = request.getHeader("X-Usuario-Id");

            if (usuarioIdHeader == null || usuarioIdHeader.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        "{\"message\":\"Cabeçalho X-Usuario-Id obrigatório.\"}"
                );
                return;
            }

            try {
                Long usuarioIdSolicitado = Long.parseLong(usuarioIdHeader);

                if (!usuarioIdSolicitado.equals(usuarioAutenticado.getId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                            "{\"message\":\"Você não pode acessar dados de outro usuário.\"}"
                    );
                    return;
                }

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        "{\"message\":\"Cabeçalho X-Usuario-Id inválido.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
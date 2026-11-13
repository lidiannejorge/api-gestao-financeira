package com.apigestaofinanceira.usuarios.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Garante que um usuário autenticado só consulte, atualize ou exclua
// a própria conta via /api/usuarios/{id}. A listagem geral
// (GET /api/usuarios) não passa por este filtro - continua
// disponível para qualquer usuário autenticado, pois não é um dado
// específico de um único usuário.
@Component
public class TitularidadeFilter extends OncePerRequestFilter {

    private static final Pattern PADRAO_ID =
            Pattern.compile("^/api/usuarios/(\\d+)$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Matcher matcher = PADRAO_ID.matcher(request.getRequestURI());

        if (matcher.matches()) {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getPrincipal()
                    instanceof UsuarioAutenticado usuarioAutenticado) {

                Long idSolicitado = Long.parseLong(matcher.group(1));

                if (!idSolicitado.equals(usuarioAutenticado.getId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                            "{\"message\":\"Você não pode acessar dados de outro usuário.\"}"
                    );
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

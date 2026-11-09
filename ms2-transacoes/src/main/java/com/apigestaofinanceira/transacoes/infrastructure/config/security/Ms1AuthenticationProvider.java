package com.apigestaofinanceira.transacoes.infrastructure.config.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class Ms1AuthenticationProvider implements AuthenticationProvider {

    private final RestClient ms1RestClient;

    public Ms1AuthenticationProvider(@Qualifier("ms1RestClient") RestClient ms1RestClient) {
        this.ms1RestClient = ms1RestClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String email = authentication.getName();
        String senha = authentication.getCredentials().toString();

        try {
            UsuarioMs1Response usuario = ms1RestClient.post().uri("/api/usuarios/autenticar").body(new AutenticarRequest(email, senha)).retrieve().body(UsuarioMs1Response.class);

            if (usuario == null || usuario.id() == null) {
                throw new BadCredentialsException("Credenciais inválidas.");
            }

            UsuarioAutenticado principal = new UsuarioAutenticado(usuario.id(), usuario.email());

            return new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        } catch (RestClientResponseException e) {

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BadCredentialsException("Credenciais inválidas.");
            }

            throw e;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private record AutenticarRequest(String email, String password) {
    }

    private record UsuarioMs1Response(Long id, String name, String email) {
    }
}
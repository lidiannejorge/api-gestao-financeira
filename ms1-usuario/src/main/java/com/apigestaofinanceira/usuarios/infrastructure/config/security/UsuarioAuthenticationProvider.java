package com.apigestaofinanceira.usuarios.infrastructure.config.security;

import com.apigestaofinanceira.usuarios.application.usecases.AutenticarUsuarioUseCase;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UsuarioAuthenticationProvider implements AuthenticationProvider {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public UsuarioAuthenticationProvider(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String email = authentication.getName();
        String senha = authentication.getCredentials().toString();

        Optional<Usuario> usuario = autenticarUsuarioUseCase.autenticar(email, senha);

        if (usuario.isEmpty()) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }

        UsuarioAutenticado principal = new UsuarioAutenticado(
                usuario.get().getId(), usuario.get().getEmail());

        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

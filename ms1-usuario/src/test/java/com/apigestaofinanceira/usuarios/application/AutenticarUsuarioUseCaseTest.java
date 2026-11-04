package com.apigestaofinanceira.usuarios.application;
import com.apigestaofinanceira.usuarios.application.services.UsuarioService;

import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.domain.ports.output.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AutenticarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private UsuarioService usuarioService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        passwordEncoder = new BCryptPasswordEncoder();

        usuarioService = new UsuarioService(
                usuarioRepositoryPort,
                passwordEncoder
        );
    }

    // 1. Email e senha corretos devem autenticar o usuário.
    @Test
    void deveAutenticarComEmailESenhaCorretos() {

        String senhaCriptografada = passwordEncoder.encode("senha1234");

        Usuario usuario = new Usuario(
                1L, "Maria", "maria@gmail.com",
                senhaCriptografada, java.time.LocalDateTime.now()
        );

        when(usuarioRepositoryPort.buscarPorEmail("maria@gmail.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.autenticar("maria@gmail.com", "senha1234");

        assertTrue(resultado.isPresent());
        assertEquals("Maria", resultado.get().getName());
    }

    // 2. Senha incorreta não deve autenticar.
    @Test
    void naoDeveAutenticarComSenhaIncorreta() {

        String senhaCriptografada = passwordEncoder.encode("senha1234");

        Usuario usuario = new Usuario(
                1L, "Maria", "maria@gmail.com",
                senhaCriptografada, java.time.LocalDateTime.now()
        );

        when(usuarioRepositoryPort.buscarPorEmail("maria@gmail.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.autenticar("maria@gmail.com", "senhaErrada");

        assertTrue(resultado.isEmpty());
    }

    // 3. Email inexistente não deve autenticar.
    @Test
    void naoDeveAutenticarComEmailInexistente() {

        when(usuarioRepositoryPort.buscarPorEmail("naoexiste@gmail.com"))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado =
                usuarioService.autenticar("naoexiste@gmail.com", "qualquerSenha");

        assertTrue(resultado.isEmpty());
    }
}
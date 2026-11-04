package com.apigestaofinanceira.usuarios.application;
import com.apigestaofinanceira.usuarios.application.services.UsuarioService;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.domain.ports.output.UsuarioRepositoryPort;
import com.apigestaofinanceira.usuarios.infrastructure.config.exception.EmailJaCadastradoException;
import com.apigestaofinanceira.usuarios.infrastructure.config.exception.UsuarioNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class UsuarioServiceTest {

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

    // 1. Cadastro com senha criptografada
    @Test
    void deveCadastrarUsuarioComSenhaCriptografada() {
        Usuario usuario = Usuario.create(
                "Maria", "maria@gmail.com", "senha1234"
        );

        when(usuarioRepositoryPort.existePorEmail(usuario.getEmail()))
                .thenReturn(false);

        when(usuarioRepositoryPort.salvar(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.executar(usuario);

        assertNotNull(resultado);
        assertEquals("Maria", resultado.getName());
        assertNotEquals("senha1234", resultado.getPassword());
        assertTrue(passwordEncoder.matches("senha1234", resultado.getPassword()));

        verify(usuarioRepositoryPort).salvar(any(Usuario.class));
    }

    // 2. Impedir cadastro com email duplicado
    @Test
    void deveImpedirCadastroDeEmailDuplicado() {
        Usuario usuario = Usuario.create(
                "Maria", "maria@gmail.com", "senha1234"
        );

        when(usuarioRepositoryPort.existePorEmail(usuario.getEmail()))
                .thenReturn(true);

        assertThrows(
                EmailJaCadastradoException.class,
                () -> usuarioService.executar(usuario)
        );

        verify(usuarioRepositoryPort, never()).salvar(any(Usuario.class));
    }

    // 3. Buscar usuário pelo ID
    @Test
    void deveBuscarUsuarioPorId() {
        Usuario usuario = Usuario.create(
                "Maria", "maria@gmail.com", "senha1234"
        );

        when(usuarioRepositoryPort.buscarPorId(1L))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Maria", resultado.get().getName());

        verify(usuarioRepositoryPort).buscarPorId(1L);
    }

    // 4. Buscar usuário inexistente
    @Test
    void deveRetornarVazioQuandoUsuarioNaoExistir() {
        when(usuarioRepositoryPort.buscarPorId(99L))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.buscarPorId(99L);

        assertTrue(resultado.isEmpty());
    }

    // 5. Listar todos os usuários
    @Test
    void deveListarTodosOsUsuarios() {
        Usuario usuario = Usuario.create(
                "Maria", "maria@gmail.com", "senha1234"
        );

        when(usuarioRepositoryPort.listarTodos())
                .thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Maria", resultado.get(0).getName());

        verify(usuarioRepositoryPort).listarTodos();
    }

    // 6. Atualizar usuário
    @Test
    void deveAtualizarUsuario() {
        Usuario usuarioExistente = Usuario.create(
                "Maria", "maria@gmail.com", "senha1234"
        );

        Usuario dadosAtualizados = Usuario.create(
                "Maria Silva", "maria.silva@gmail.com", "senha1234"
        );

        when(usuarioRepositoryPort.buscarPorId(1L))
                .thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepositoryPort.buscarPorEmail(dadosAtualizados.getEmail()))
                .thenReturn(Optional.empty());

        when(usuarioRepositoryPort.salvar(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.atualizar(1L, dadosAtualizados);

        assertEquals("Maria Silva", resultado.getName());
        assertEquals("maria.silva@gmail.com", resultado.getEmail());

        verify(usuarioRepositoryPort).salvar(usuarioExistente);
    }

    // 7. Impedir atualização com email de outro usuário


    @Test
    void deveImpedirAtualizacaoComEmailDuplicado() {

        Usuario usuarioExistente = new Usuario(
                1L,
                "Maria",
                "maria@gmail.com",
                "senha1234",
                java.time.LocalDateTime.now()
        );

        Usuario outroUsuario = new Usuario(
                2L,
                "João",
                "joao@gmail.com",
                "senha1234",
                java.time.LocalDateTime.now()
        );

        when(usuarioRepositoryPort.buscarPorId(1L))
                .thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepositoryPort.buscarPorEmail("joao@gmail.com"))
                .thenReturn(Optional.of(outroUsuario));

        assertThrows(
                EmailJaCadastradoException.class,
                () -> usuarioService.atualizar(1L, outroUsuario)
        );

        verify(usuarioRepositoryPort, never()).salvar(any(Usuario.class));
    }


    // 8. Impedir atualização de usuário inexistente
    @Test
    void deveImpedirAtualizacaoDeUsuarioInexistente() {
        Usuario usuario = Usuario.create(
                "Maria", "maria@gmail.com", "senha1234"
        );

        when(usuarioRepositoryPort.buscarPorId(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> usuarioService.atualizar(99L, usuario)
        );
    }

    // 9. Excluir usuário
    @Test
    void deveExcluirUsuario() {
        Usuario usuario = Usuario.create(
                "Maria", "maria@gmail.com", "senha1234"
        );

        when(usuarioRepositoryPort.buscarPorId(1L))
                .thenReturn(Optional.of(usuario));

        usuarioService.excluir(1L);

        verify(usuarioRepositoryPort).excluir(1L);
    }

    // 10. Impedir exclusão de usuário inexistente
    @Test
    void deveImpedirExclusaoDeUsuarioInexistente() {
        when(usuarioRepositoryPort.buscarPorId(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> usuarioService.excluir(99L)
        );

        verify(usuarioRepositoryPort, never()).excluir(99L);
    }
}
package com.apigestaofinanceira.usuarios.application;
import com.apigestaofinanceira.usuarios.application.services.ImportarUsuariosCsvService;
import com.apigestaofinanceira.usuarios.application.usecases.*;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.controller.UsuarioController;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.CreateUsuarioRequestDTO;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.ImportacaoUsuarioResponseDTO;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.UsuarioResponseDTO;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.mapper.UsuarioWebMapper;
import com.apigestaofinanceira.usuarios.infrastructure.config.exception.UsuarioNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private CreateUsuarioUseCase createUsuarioUseCase;

    @Mock
    private FindUsuarioUseCase findUsuarioUseCase;

    @Mock
    private UpdateUsuarioUseCase updateUsuarioUseCase;

    @Mock
    private DeleteUsuarioUseCase deleteUsuarioUseCase;

    @Mock
    private UsuarioWebMapper usuarioWebMapper;

    @Mock
    private ImportarUsuariosCsvService importarUsuariosCsvService;

    @Mock
    private CreateUsuarioRequestDTO request;

    @Mock
    private UsuarioResponseDTO response;

    @Mock
    private ImportacaoUsuarioResponseDTO importacaoResponse;

    @Mock
    private AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    private UsuarioController controller;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        controller = new UsuarioController(
                createUsuarioUseCase,
                findUsuarioUseCase,
                updateUsuarioUseCase,
                deleteUsuarioUseCase,
                usuarioWebMapper,
                importarUsuariosCsvService,
                autenticarUsuarioUseCase

        );

        usuario = new Usuario(
                1L,
                "Ana",
                "ana@gmail.com",
                "senha1234",
                LocalDateTime.now()
        );
    }

    @Test
    void deveCriarUsuario() {
        when(usuarioWebMapper.toDomain(request)).thenReturn(usuario);
        when(createUsuarioUseCase.executar(usuario)).thenReturn(usuario);
        when(usuarioWebMapper.toResponse(usuario)).thenReturn(response);

        var resultado = controller.criar(request);

        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());
        assertSame(response, resultado.getBody());
    }

    @Test
    void deveImportarUsuarios() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "usuarios.csv",
                "text/csv",
                "name,email,password".getBytes()
        );

        when(importarUsuariosCsvService.importar(arquivo))
                .thenReturn(importacaoResponse);

        var resultado = controller.importarUsuarios(arquivo);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertSame(importacaoResponse, resultado.getBody());
    }

    @Test
    void deveListarUsuarios() {
        when(findUsuarioUseCase.listarTodos())
                .thenReturn(List.of(usuario));

        when(usuarioWebMapper.toResponse(usuario))
                .thenReturn(response);

        var resultado = controller.listar();

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertNotNull(resultado.getBody());
        assertEquals(1, resultado.getBody().size());
        assertSame(response, resultado.getBody().get(0));
    }

    @Test
    void deveBuscarUsuarioPorId() {
        when(findUsuarioUseCase.buscarPorId(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioWebMapper.toResponse(usuario))
                .thenReturn(response);

        var resultado = controller.buscarPorId(1L);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertSame(response, resultado.getBody());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(findUsuarioUseCase.buscarPorId(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNotFoundException.class,
                () -> controller.buscarPorId(99L)
        );
    }

    @Test
    void deveAtualizarUsuario() {
        when(usuarioWebMapper.toDomain(request)).thenReturn(usuario);
        when(updateUsuarioUseCase.atualizar(1L, usuario))
                .thenReturn(usuario);
        when(usuarioWebMapper.toResponse(usuario))
                .thenReturn(response);

        var resultado = controller.atualizar(1L, request);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertSame(response, resultado.getBody());
    }

    @Test
    void deveExcluirUsuario() {
        var resultado = controller.excluir(1L);

        assertEquals(HttpStatus.NO_CONTENT, resultado.getStatusCode());
        assertNull(resultado.getBody());

        verify(deleteUsuarioUseCase).excluir(1L);
    }
}
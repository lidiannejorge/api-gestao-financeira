
package com.apigestaofinanceira.usuarios.application;


import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.UsuarioPersistenceAdapter;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.entity.UsuarioEntity;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.repository.SpringDataUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioPersistenceAdapterTest {

    @Mock
    private SpringDataUsuarioRepository repository;

    private UsuarioPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UsuarioPersistenceAdapter(repository);
    }

    private UsuarioEntity criarEntity() {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(1L);
        entity.setName("Ana");
        entity.setEmail("ana@gmail.com");
        entity.setPassword("senha1234");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    @Test
    void deveSalvarUsuario() {
        Usuario usuario = new Usuario(
                null, "Ana", "ana@gmail.com", "senha1234",
                LocalDateTime.now()
        );

        when(repository.save(any(UsuarioEntity.class)))
                .thenReturn(criarEntity());

        Usuario resultado = adapter.salvar(usuario);

        assertEquals(1L, resultado.getId());
        assertEquals("Ana", resultado.getName());
        verify(repository).save(any(UsuarioEntity.class));
    }

    @Test
    void deveBuscarUsuarioPorId() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(criarEntity()));

        Optional<Usuario> resultado = adapter.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Ana", resultado.get().getName());
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExistir() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = adapter.buscarPorId(99L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        when(repository.findByEmail("ana@gmail.com"))
                .thenReturn(Optional.of(criarEntity()));

        Optional<Usuario> resultado =
                adapter.buscarPorEmail("ana@gmail.com");

        assertTrue(resultado.isPresent());
        assertEquals("ana@gmail.com", resultado.get().getEmail());
    }

    @Test
    void deveRetornarVazioQuandoEmailNaoExistir() {
        when(repository.findByEmail("naoexiste@gmail.com"))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado =
                adapter.buscarPorEmail("naoexiste@gmail.com");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveListarUsuarios() {
        when(repository.findAll())
                .thenReturn(List.of(criarEntity()));

        List<Usuario> resultado = adapter.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Ana", resultado.get(0).getName());
    }

    @Test
    void deveExcluirUsuario() {
        adapter.excluir(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deveVerificarSeEmailExiste() {
        when(repository.existsByEmail("ana@gmail.com"))
                .thenReturn(true);

        assertTrue(adapter.existePorEmail("ana@gmail.com"));
    }

    @Test
    void deveRetornarFalsoQuandoEmailNaoExiste() {
        when(repository.existsByEmail("outro@gmail.com"))
                .thenReturn(false);

        assertFalse(adapter.existePorEmail("outro@gmail.com"));
    }
}
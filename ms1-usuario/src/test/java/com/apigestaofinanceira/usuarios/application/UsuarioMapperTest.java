package com.apigestaofinanceira.usuarios.application;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.entity.UsuarioEntity;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.mapper.UsuarioMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    @Test
    void deveConverterUsuarioParaEntity() {
        LocalDateTime data = LocalDateTime.now();

        Usuario usuario = new Usuario(
                1L, "Ana", "ana@gmail.com", "senha1234", data
        );

        UsuarioEntity entity = UsuarioMapper.toEntity(usuario);

        assertEquals(1L, entity.getId());
        assertEquals("Ana", entity.getName());
        assertEquals("ana@gmail.com", entity.getEmail());
        assertEquals("senha1234", entity.getPassword());
        assertEquals(data, entity.getCreatedAt());
    }

    @Test
    void deveConverterEntityParaUsuario() {
        LocalDateTime data = LocalDateTime.now();

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(1L);
        entity.setName("Ana");
        entity.setEmail("ana@gmail.com");
        entity.setPassword("senha1234");
        entity.setCreatedAt(data);

        Usuario usuario = UsuarioMapper.toDomain(entity);

        assertEquals(1L, usuario.getId());
        assertEquals("Ana", usuario.getName());
        assertEquals("ana@gmail.com", usuario.getEmail());
        assertEquals("senha1234", usuario.getPassword());
        assertEquals(data, usuario.getCreatedAt());
    }
}
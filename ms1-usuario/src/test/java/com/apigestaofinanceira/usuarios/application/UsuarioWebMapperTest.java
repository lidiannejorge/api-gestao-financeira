package com.apigestaofinanceira.usuarios.application;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.CreateUsuarioRequestDTO;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.UsuarioResponseDTO;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.mapper.UsuarioWebMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioWebMapperTest {

    private final UsuarioWebMapper mapper = new UsuarioWebMapper();

    @Test
    void deveConverterRequestDTOParaUsuario() {

        CreateUsuarioRequestDTO dto = new CreateUsuarioRequestDTO();

        dto.setName("Ana");
        dto.setEmail("ana@gmail.com");
        dto.setPassword("senha1234");

        Usuario usuario = mapper.toDomain(dto);

        assertEquals("Ana", usuario.getName());
        assertEquals("ana@gmail.com", usuario.getEmail());
        assertEquals("senha1234", usuario.getPassword());
        assertNotNull(usuario.getCreatedAt());
        assertNull(usuario.getId());
    }

    @Test
    void deveConverterUsuarioParaResponseDTO() {

        LocalDateTime data = LocalDateTime.now();

        Usuario usuario = new Usuario(
                1L,
                "Ana",
                "ana@gmail.com",
                "senha1234",
                data
        );

        UsuarioResponseDTO dto = mapper.toResponse(usuario);

        assertEquals(1L, dto.getId());
        assertEquals("Ana", dto.getName());
        assertEquals("ana@gmail.com", dto.getEmail());
        assertEquals(data, dto.getCreatedAt());
    }
}
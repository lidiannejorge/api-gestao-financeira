package com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto;

import java.time.LocalDateTime;

public class UsuarioResponseDTO {

    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(
            Long id,
            String name,
            String email,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
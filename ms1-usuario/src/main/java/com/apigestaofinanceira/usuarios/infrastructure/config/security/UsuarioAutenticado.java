package com.apigestaofinanceira.usuarios.infrastructure.config.security;

public class UsuarioAutenticado {

    private final Long id;
    private final String email;

    public UsuarioAutenticado(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}

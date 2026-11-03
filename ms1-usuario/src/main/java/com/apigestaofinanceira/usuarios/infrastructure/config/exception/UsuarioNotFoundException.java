package com.apigestaofinanceira.usuarios.infrastructure.config.exception;

public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(Long id) {
        super("Usuário não encontrado com o ID: " + id);
    }
}
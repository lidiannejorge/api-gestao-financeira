package com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AutenticarRequestDTO {

    @Schema(example = "maria.silva@gmail.com")
    @NotBlank(message = "O email é obrigatório.")
    private String email;

    @Schema(example = "senha1234")
    @NotBlank(message = "A senha é obrigatória.")
    private String password;

    public AutenticarRequestDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
package com.apigestaofinanceira.usuarios.domain.model;

import java.time.LocalDateTime;

public class Usuario {
    private Long id;
    private String name;
    private String email;
    private String password;
    private LocalDateTime createdAt;


    public Usuario(Long id, String name, String email, String password, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    public static Usuario create(
            String name,
            String email,
            String password

){
        return  new Usuario(null, name, email, password, LocalDateTime.now());
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

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void update(String name, String email){
        this.name = name;
        this.email = email;
    }
}

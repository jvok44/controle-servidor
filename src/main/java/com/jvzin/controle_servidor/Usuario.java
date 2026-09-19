package com.jvzin.controle_servidor;

public class Usuario {

    private final String username;
    private final String senhaHash;

    public Usuario(
            String username,
            String senhaHash
    ) {
        this.username = username;
        this.senhaHash = senhaHash;
    }

    public String getUsername() {
        return username;
    }

    public String getSenhaHash() {
        return senhaHash;
    }
}
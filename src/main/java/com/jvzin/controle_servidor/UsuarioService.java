package com.jvzin.controle_servidor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;

    private final Map<String, Usuario> usuarios =
            new ConcurrentHashMap<>();


    public UsuarioService(
            PasswordEncoder passwordEncoder
    ) {

        this.passwordEncoder = passwordEncoder;
    }


    public boolean existe(String username) {

        return usuarios.containsKey(username);
    }


    public Usuario criarUsuario(
            String username,
            String senha
    ) {

        String senhaHash =
                passwordEncoder.encode(senha);

        Usuario usuario =
                new Usuario(
                        username,
                        senhaHash
                );

        usuarios.put(
                username,
                usuario
        );

        return usuario;
    }


    public Usuario buscar(
            String username
    ) {

        return usuarios.get(username);
    }


    public boolean verificarSenha(
            Usuario usuario,
            String senha
    ) {

        return passwordEncoder.matches(
                senha,
                usuario.getSenhaHash()
        );
    }
}
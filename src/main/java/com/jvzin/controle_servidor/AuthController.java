package com.jvzin.controle_servidor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final TokenService tokenService;


    public AuthController(
            UsuarioService usuarioService,
            TokenService tokenService
    ) {

        this.usuarioService =
                usuarioService;

        this.tokenService =
                tokenService;
    }


    // =========================================================
    // CRIAR USUÁRIO
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> registrar(
            @RequestBody LoginRequest request
    ) {

        if (
                request.username() == null
                        ||
                request.username().isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body("Username obrigatório");
        }


        if (
                request.senha() == null
                        ||
                request.senha().length() < 8
        ) {

            return ResponseEntity
                    .badRequest()
                    .body("A senha precisa ter pelo menos 8 caracteres");
        }


        if (
                usuarioService.existe(
                        request.username()
                )
        ) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Usuário já existe");
        }


        usuarioService.criarUsuario(
                request.username(),
                request.senha()
        );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Usuário criado");
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        Usuario usuario =
                usuarioService.buscar(
                        request.username()
                );


        if (
                usuario == null
                        ||
                !usuarioService.verificarSenha(
                        usuario,
                        request.senha()
                )
        ) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário ou senha inválidos");
        }


        String token =
                tokenService.criarToken(
                        usuario.getUsername()
                );


        return ResponseEntity.ok(
                new LoginResponse(
                        usuario.getUsername(),
                        token
                )
        );
    }


    // =========================================================
    // DTOs
    // =========================================================

    public record LoginRequest(
            String username,
            String senha
    ) {}


    public record LoginResponse(
            String username,
            String token
    ) {}
}
package com.jvzin.controle_servidor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dispositivos")
public class DispositivoController {

    private final DispositivoService dispositivoService;


    public DispositivoController(
            DispositivoService dispositivoService
    ) {

        this.dispositivoService =
                dispositivoService;
    }


    // =========================================================
    // REGISTRAR NOVO CELULAR
    // =========================================================

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar() {

        DispositivoService.RegistroDispositivo registro =
                dispositivoService.registrar();


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registro);
    }


    // =========================================================
    // PAREAR COM A CONTA
    // =========================================================

    @PostMapping("/parear")
    public ResponseEntity<?> parear(
            @RequestBody CodigoRequest request,
            Authentication authentication
    ) {

        String username =
                authentication.getName();


        boolean sucesso =
                dispositivoService.parear(
                        request.codigo(),
                        username
                );


        if (!sucesso) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Código inválido, expirado ou dispositivo já pareado"
                    );
        }


        return ResponseEntity.ok(
                "Dispositivo pareado com sucesso"
        );
    }


    // =========================================================
    // AUTENTICAR DISPOSITIVO
    // =========================================================

    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticar(
            @RequestBody AutenticacaoDispositivoRequest request
    ) {

        String deviceSessionToken =
                dispositivoService.autenticarDispositivo(
                        request.deviceId(),
                        request.deviceToken()
                );


        if (deviceSessionToken == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Dispositivo não autenticado"
                    );
        }


        return ResponseEntity.ok(
                new SessaoDispositivoResponse(
                        deviceSessionToken
                )
        );
    }


    // =========================================================
    // CONSULTAR DISPOSITIVO
    // =========================================================

    @GetMapping("/{deviceId}")
    public ResponseEntity<?> obter(
            @PathVariable String deviceId,
            Authentication authentication
    ) {

        String username =
                authentication.getName();


        if (
                !dispositivoService.usuarioTemAcesso(
                        username,
                        deviceId
                )
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Você não tem acesso a este dispositivo"
                    );
        }


        return ResponseEntity.ok(
                dispositivoService.buscar(
                        deviceId
                )
        );
    }


    // =========================================================
    // REQUEST DE PAREAMENTO
    // =========================================================

    public record CodigoRequest(
            String codigo
    ) {}


    // =========================================================
    // REQUEST DE AUTENTICAÇÃO DO DISPOSITIVO
    // =========================================================

    public record AutenticacaoDispositivoRequest(
            String deviceId,
            String deviceToken
    ) {}


    // =========================================================
    // RESPOSTA DA AUTENTICAÇÃO
    // =========================================================

    public record SessaoDispositivoResponse(
            String deviceSessionToken
    ) {}
}
package com.jvzin.controle_servidor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dispositivos")
public class DispositivoController {

    private final DispositivoService dispositivoService;

    public DispositivoController(
            DispositivoService dispositivoService
    ) {
        this.dispositivoService = dispositivoService;
    }

    @GetMapping("/conectar")
    public ResponseEntity<?> conectar(
            @RequestParam String codigo
    ) {

        if (!dispositivoService.codigoValido(codigo)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Código inválido");
        }

        return ResponseEntity.ok(
                "Conectado com sucesso"
        );
    }
}
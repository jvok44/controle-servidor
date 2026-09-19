package com.jvzin.controle_servidor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/dispositivos")
public class ComandoController {

    private final DispositivoService dispositivoService;

    private final Map<String, EstadoDispositivo> dispositivos =
            new ConcurrentHashMap<>();


    public ComandoController(
            DispositivoService dispositivoService
    ) {

        this.dispositivoService =
                dispositivoService;
    }


    // =========================================================
    // ESTADO DO DISPOSITIVO
    // =========================================================

    private EstadoDispositivo obterEstado(
            String deviceId
    ) {

        return dispositivos.computeIfAbsent(
                deviceId,
                id -> new EstadoDispositivo()
        );
    }


    // =========================================================
    // VERIFICAR SE É AUTENTICAÇÃO DO DISPOSITIVO
    // =========================================================

    private boolean ehDispositivo(
            Authentication authentication,
            String deviceId
    ) {

        if (authentication == null) {
            return false;
        }

        return authentication
                .getName()
                .equals("DEVICE:" + deviceId);
    }


    // =========================================================
    // VERIFICAR ACESSO DA CONTA
    // =========================================================

    private boolean usuarioTemAcesso(
            Authentication authentication,
            String deviceId
    ) {

        if (authentication == null) {
            return false;
        }


        // Dispositivo não pode usar essa autorização
        if (ehDispositivo(authentication, deviceId)) {
            return false;
        }


        return dispositivoService.usuarioTemAcesso(
                authentication.getName(),
                deviceId
        );
    }


    // =========================================================
    // CONTA → ENVIAR COMANDO
    // =========================================================

    @PostMapping("/{deviceId}/comando")
    public ResponseEntity<?> enviarComando(
            @PathVariable String deviceId,
            @RequestParam String comando,
            Authentication authentication
    ) {

        if (!usuarioTemAcesso(
                authentication,
                deviceId
        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Você não tem acesso a este dispositivo"
                    );
        }


        EstadoDispositivo estado =
                obterEstado(deviceId);


        synchronized (estado) {

            estado.ultimoComando =
                    comando;
        }


        return ResponseEntity.ok(
                "Comando recebido: " + comando
        );
    }


    // =========================================================
    // CONTA → ENVIAR COMANDO PELA URL
    // =========================================================

    @GetMapping("/{deviceId}/enviar")
    public ResponseEntity<?> enviarComandoPelaUrl(
            @PathVariable String deviceId,
            @RequestParam String comando,
            Authentication authentication
    ) {

        if (!usuarioTemAcesso(
                authentication,
                deviceId
        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Você não tem acesso a este dispositivo"
                    );
        }


        EstadoDispositivo estado =
                obterEstado(deviceId);


        synchronized (estado) {

            estado.ultimoComando =
                    comando;
        }


        return ResponseEntity.ok(
                "Comando enviado: " + comando
        );
    }


    // =========================================================
    // CELULAR → BUSCAR COMANDO
    // =========================================================

    @GetMapping("/{deviceId}/comando")
    public ResponseEntity<?> obterComando(
            @PathVariable String deviceId,
            Authentication authentication
    ) {

        if (!ehDispositivo(
                authentication,
                deviceId
        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Apenas o dispositivo pode acessar esta rota"
                    );
        }


        EstadoDispositivo estado =
                obterEstado(deviceId);


        synchronized (estado) {

            String comando =
                    estado.ultimoComando;


            estado.ultimoComando =
                    "";


            return ResponseEntity.ok(
                    comando
            );
        }
    }


    // =========================================================
    // CELULAR → ENVIAR STATUS
    // =========================================================

    @PostMapping("/{deviceId}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable String deviceId,
            @RequestParam int bateria,
            @RequestParam String conexao,
            Authentication authentication
    ) {

        if (!ehDispositivo(
                authentication,
                deviceId
        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Apenas o dispositivo pode enviar seu status"
                    );
        }


        EstadoDispositivo estado =
                obterEstado(deviceId);


        synchronized (estado) {

            estado.bateria =
                    bateria;

            estado.conexao =
                    conexao;

            estado.ultimaComunicacao =
                    System.currentTimeMillis();
        }


        return ResponseEntity.ok(
                "Status atualizado"
        );
    }


    // =========================================================
    // CONTA → CONSULTAR STATUS
    // =========================================================

    @GetMapping("/{deviceId}/status")
    public ResponseEntity<?> obterStatus(
            @PathVariable String deviceId,
            Authentication authentication
    ) {

        if (!usuarioTemAcesso(
                authentication,
                deviceId
        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Você não tem acesso a este dispositivo"
                    );
        }


        EstadoDispositivo estado =
                obterEstado(deviceId);


        synchronized (estado) {

            long agora =
                    System.currentTimeMillis();


            boolean celularOnline =
                    estado.ultimaComunicacao > 0
                    &&
                    agora - estado.ultimaComunicacao < 15000;


            return ResponseEntity.ok(
                    new StatusResponse(
                            celularOnline,
                            estado.bateria,
                            estado.conexao,
                            estado.ultimaComunicacao
                    )
            );
        }
    }


    // =========================================================
    // CELULAR → ENVIAR LOCALIZAÇÃO
    // =========================================================

    @PostMapping("/{deviceId}/localizacao")
    public ResponseEntity<?> atualizarLocalizacao(
            @PathVariable String deviceId,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam float precisao,
            Authentication authentication
    ) {

        if (!ehDispositivo(
                authentication,
                deviceId
        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Apenas o dispositivo pode enviar sua localização"
                    );
        }


        EstadoDispositivo estado =
                obterEstado(deviceId);


        synchronized (estado) {

            estado.latitude =
                    latitude;

            estado.longitude =
                    longitude;

            estado.precisao =
                    precisao;

            estado.ultimaComunicacao =
                    System.currentTimeMillis();
        }


        return ResponseEntity.ok(
                "Localização atualizada"
        );
    }


    // =========================================================
    // CONTA → CONSULTAR LOCALIZAÇÃO
    // =========================================================

    @GetMapping("/{deviceId}/localizacao")
    public ResponseEntity<?> obterLocalizacao(
            @PathVariable String deviceId,
            Authentication authentication
    ) {

        if (!usuarioTemAcesso(
                authentication,
                deviceId
        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Você não tem acesso a este dispositivo"
                    );
        }


        EstadoDispositivo estado =
                obterEstado(deviceId);


        synchronized (estado) {

            return ResponseEntity.ok(
                    new LocalizacaoResponse(
                            estado.latitude,
                            estado.longitude,
                            estado.precisao
                    )
            );
        }
    }


    // =========================================================
    // ESTADO INTERNO
    // =========================================================

    private static class EstadoDispositivo {

        String ultimoComando = "";

        int bateria = -1;

        String conexao = "desconhecida";

        double latitude = 0;

        double longitude = 0;

        float precisao = 0;

        long ultimaComunicacao = 0;
    }


    // =========================================================
    // RESPOSTA DO STATUS
    // =========================================================

    public static class StatusResponse {

        public boolean online;

        public int bateria;

        public String conexao;

        public long ultimaComunicacao;


        public StatusResponse(
                boolean online,
                int bateria,
                String conexao,
                long ultimaComunicacao
        ) {

            this.online =
                    online;

            this.bateria =
                    bateria;

            this.conexao =
                    conexao;

            this.ultimaComunicacao =
                    ultimaComunicacao;
        }
    }


    // =========================================================
    // RESPOSTA DA LOCALIZAÇÃO
    // =========================================================

    public static class LocalizacaoResponse {

        public double latitude;

        public double longitude;

        public float precisao;


        public LocalizacaoResponse(
                double latitude,
                double longitude,
                float precisao
        ) {

            this.latitude =
                    latitude;

            this.longitude =
                    longitude;

            this.precisao =
                    precisao;
        }
    }
}
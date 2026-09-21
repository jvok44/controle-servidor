package com.jvzin.controle_servidor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/dispositivos")
public class ComandoController {

    private final DispositivoService dispositivoService;

    private final EstadoDispositivo estado =
            new EstadoDispositivo();


    public ComandoController(
            DispositivoService dispositivoService
    ) {
        this.dispositivoService =
                dispositivoService;
    }


    // =========================================================
    // VERIFICAR CÓDIGO
    // =========================================================

    private boolean codigoValido(
            String codigo
    ) {

        return dispositivoService.codigoValido(
                codigo
        );
    }


    // =========================================================
    // ENVIAR COMANDO
    // =========================================================

    @GetMapping("/comando")
    public ResponseEntity<?> enviarComando(
            @RequestParam String codigo,
            @RequestParam String comando
    ) {

        if (!codigoValido(codigo)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Código inválido");
        }


        synchronized (estado) {

            estado.ultimoComando =
                    comando;
        }


        return ResponseEntity.ok(
                "Comando enviado: " + comando
        );
    }


    // =========================================================
    // CELULAR BUSCA COMANDO
    // =========================================================

    @GetMapping("/buscar-comando")
    public ResponseEntity<?> buscarComando(
            @RequestParam String codigo
    ) {

        if (!codigoValido(codigo)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Código inválido");
        }


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
    // ATUALIZAR STATUS
    // =========================================================

    @PostMapping("/status")
    public ResponseEntity<?> atualizarStatus(
            @RequestParam String codigo,
            @RequestParam int bateria,
            @RequestParam String conexao
    ) {

        if (!codigoValido(codigo)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Código inválido");
        }


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
    // OBTER STATUS
    // =========================================================

    @GetMapping("/status")
    public ResponseEntity<?> obterStatus(
            @RequestParam String codigo
    ) {

        if (!codigoValido(codigo)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Código inválido");
        }


        synchronized (estado) {

            long agora =
                    System.currentTimeMillis();


            boolean online =
                    estado.ultimaComunicacao > 0
                            &&
                    agora - estado.ultimaComunicacao < 15000;


            return ResponseEntity.ok(
                    new StatusResponse(
                            online,
                            estado.bateria,
                            estado.conexao,
                            estado.ultimaComunicacao
                    )
            );
        }
    }


    // =========================================================
    // ATUALIZAR LOCALIZAÇÃO
    // =========================================================

    @PostMapping("/localizacao")
    public ResponseEntity<?> atualizarLocalizacao(
            @RequestParam String codigo,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam float precisao
    ) {

        if (!codigoValido(codigo)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Código inválido");
        }


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
    // OBTER LOCALIZAÇÃO
    // =========================================================

    @GetMapping("/localizacao")
    public ResponseEntity<?> obterLocalizacao(
            @RequestParam String codigo
    ) {

        if (!codigoValido(codigo)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Código inválido");
        }


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
    // ESTADO DO CELULAR
    // =========================================================

    private static class EstadoDispositivo {

        String ultimoComando = "";

        int bateria = -1;

        String conexao =
                "desconhecida";

        double latitude = 0;

        double longitude = 0;

        float precisao = 0;

        long ultimaComunicacao = 0;
    }


    // =========================================================
    // RESPOSTA STATUS
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
    // RESPOSTA LOCALIZAÇÃO
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
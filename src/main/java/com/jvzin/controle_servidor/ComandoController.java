package com.jvzin.controle_servidor; import 
org.springframework.web.bind.annotation.*; @RestController public class 
ComandoController {
    private String ultimoComando = ""; private boolean online = false; 
    private int bateria = -1; private String conexao = "desconhecida"; 
    private double latitude = 0; private double longitude = 0; private 
    float precisao = 0; private long ultimaComunicacao = 0;
    // ========================================================= ENVIAR 
    // COMANDO =========================================================
    @PostMapping("/comando") public String enviarComando( @RequestParam 
            String comando
    ) { ultimoComando = comando; return "Comando recebido: " + comando;
    }
    // ========================================================= ENVIAR 
    // COMANDO PELA URL 
    // =========================================================
    @GetMapping("/enviar") public String enviarComandoPelaUrl( 
            @RequestParam String comando
    ) { ultimoComando = comando; return "Comando enviado: " + comando;
    }
    // ========================================================= CELULAR 
    // BUSCA COMANDO 
    // =========================================================
    @GetMapping("/comando") public synchronized String obterComando() { 
        String comando = ultimoComando; ultimoComando = ""; return 
        comando;
    }
    // ========================================================= 
    // ATUALIZAR STATUS 
    // =========================================================
    @PostMapping("/status") public synchronized String atualizarStatus( 
            @RequestParam int bateria, @RequestParam String conexao
    ) { this.online = true; this.bateria = bateria; this.conexao = 
        conexao; this.ultimaComunicacao =
                System.currentTimeMillis(); return "Status atualizado";
    }
    // ========================================================= 
    // CONSULTAR STATUS 
    // =========================================================
    @GetMapping("/status") public synchronized StatusResponse 
    obterStatus() {
        long agora = System.currentTimeMillis(); boolean celularOnline = 
                ultimaComunicacao > 0
                        && agora - ultimaComunicacao < 15000; return new 
        StatusResponse(
                celularOnline, bateria, conexao, ultimaComunicacao );
    }
    // ========================================================= 
    // ATUALIZAR LOCALIZAÇÃO 
    // =========================================================
    @PostMapping("/localizacao") public synchronized String 
    atualizarLocalizacao(
            @RequestParam double latitude, @RequestParam double 
            longitude, @RequestParam float precisao
    ) { this.latitude = latitude; this.longitude = longitude; 
        this.precisao = precisao; this.ultimaComunicacao =
                System.currentTimeMillis(); return "Localização 
        atualizada";
    }
    // ========================================================= 
    // CONSULTAR LOCALIZAÇÃO 
    // =========================================================
    @GetMapping("/localizacao") public synchronized LocalizacaoResponse 
    obterLocalizacao() {
        return new LocalizacaoResponse( latitude, longitude, precisao );
    }
    // ========================================================= 
    // RESPOSTA DO STATUS 
    // =========================================================
    public static class StatusResponse { public boolean online; public 
        int bateria; public String conexao; public long 
        ultimaComunicacao; public StatusResponse(
                boolean online, int bateria, String conexao, long 
                ultimaComunicacao
        ) { this.online = online; this.bateria = bateria; this.conexao = 
            conexao; this.ultimaComunicacao =
                    ultimaComunicacao;
        }
    }
    // ========================================================= 
    // RESPOSTA DA LOCALIZAÇÃO 
    // =========================================================
    public static class LocalizacaoResponse { public double latitude; 
        public double longitude; public float precisao; public 
        LocalizacaoResponse(
                double latitude, double longitude, float precisao ) { 
            this.latitude = latitude; this.longitude = longitude; 
            this.precisao = precisao;
        }
    }
}

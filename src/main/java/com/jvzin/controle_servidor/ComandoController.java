package com.jvzin.controle_servidor; import 
org.springframework.web.bind.annotation.*; @RestController public class 
ComandoController {
    private String ultimoComando = ""; @PostMapping("/comando") public 
    String enviarComando(
            @RequestParam String comando ) { ultimoComando = comando; 
        return "Comando recebido: " + comando;
    }
    @GetMapping("/comando") public String obterComando() { String 
        comando = ultimoComando; ultimoComando = ""; return comando;
    }
    @GetMapping("/enviar") public String enviarComandoPelaUrl( 
            @RequestParam String comando
    ) { ultimoComando = comando; return "Comando enviado: " + comando;
    }
}

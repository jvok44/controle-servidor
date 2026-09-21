package com.jvzin.controle_servidor;

import org.springframework.stereotype.Service;

@Service
public class DispositivoService {

    private static final String CODIGO_ACESSO = "402671";

    public boolean codigoValido(String codigo) {
        return CODIGO_ACESSO.equals(codigo);
    }
}
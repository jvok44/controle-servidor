package com.jvzin.controle_servidor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DispositivoService {

    private final Map<String, Dispositivo> dispositivos =
            new ConcurrentHashMap<>();

    private final Map<String, CodigoPareamento> codigos =
            new ConcurrentHashMap<>();

    private final Map<String, String> sessoesDispositivo =
            new ConcurrentHashMap<>();

    private final SecureRandom random =
            new SecureRandom();

    private final PasswordEncoder passwordEncoder;


    public DispositivoService(
            PasswordEncoder passwordEncoder
    ) {

        this.passwordEncoder =
                passwordEncoder;
    }


    // =========================================================
    // REGISTRAR NOVO DISPOSITIVO
    // =========================================================

    public RegistroDispositivo registrar() {

        String deviceId =
                UUID.randomUUID().toString();


        byte[] bytes =
                new byte[32];

        random.nextBytes(bytes);


        String deviceToken =
                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);


        String deviceTokenHash =
                passwordEncoder.encode(
                        deviceToken
                );


        Dispositivo dispositivo =
                new Dispositivo(
                        deviceId,
                        deviceTokenHash
                );


        dispositivos.put(
                deviceId,
                dispositivo
        );


        String codigo =
                gerarCodigoPareamento(
                        deviceId
                );


        return new RegistroDispositivo(
                deviceId,
                deviceToken,
                codigo
        );
    }


    // =========================================================
    // BUSCAR DISPOSITIVO
    // =========================================================

    public Dispositivo buscar(
            String deviceId
    ) {

        return dispositivos.get(deviceId);
    }


    // =========================================================
    // VERIFICAR DONO
    // =========================================================

    public boolean usuarioTemAcesso(
            String username,
            String deviceId
    ) {

        Dispositivo dispositivo =
                dispositivos.get(deviceId);


        if (dispositivo == null) {
            return false;
        }


        if (dispositivo.getUsername() == null) {
            return false;
        }


        return dispositivo
                .getUsername()
                .equals(username);
    }


    // =========================================================
    // AUTENTICAR DISPOSITIVO
    // =========================================================

    public String autenticarDispositivo(
            String deviceId,
            String deviceToken
    ) {

        Dispositivo dispositivo =
                dispositivos.get(deviceId);


        if (dispositivo == null) {
            return null;
        }


        boolean correto =
                passwordEncoder.matches(
                        deviceToken,
                        dispositivo.getDeviceTokenHash()
                );


        if (!correto) {
            return null;
        }


        // =====================================================
        // GERAR TOKEN DE SESSÃO DO DISPOSITIVO
        // =====================================================

        byte[] bytes =
                new byte[32];

        random.nextBytes(bytes);


        String deviceSessionToken =
                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);


        sessoesDispositivo.put(
                deviceId,
                deviceSessionToken
        );


        return deviceSessionToken;
    }


    // =========================================================
    // VERIFICAR SESSÃO DO DISPOSITIVO
    // =========================================================

    public boolean sessaoDispositivoValida(
            String deviceId,
            String deviceSessionToken
    ) {

        String tokenAtual =
                sessoesDispositivo.get(
                        deviceId
                );


        if (tokenAtual == null) {
            return false;
        }


        return tokenAtual.equals(
                deviceSessionToken
        );
    }


    // =========================================================
    // ENCERRAR SESSÃO DO DISPOSITIVO
    // =========================================================

    public void encerrarSessao(
            String deviceId
    ) {

        sessoesDispositivo.remove(
                deviceId
        );
    }


    // =========================================================
    // GERAR CÓDIGO DE PAREAMENTO
    // =========================================================

    private String gerarCodigoPareamento(
            String deviceId
    ) {

        String codigo;

        do {

            codigo =
                    String.format(
                            "%06d",
                            random.nextInt(1_000_000)
                    );

        } while (
                codigos.containsKey(codigo)
        );


        long expiracao =
                System.currentTimeMillis()
                        + 5 * 60 * 1000;


        codigos.put(
                codigo,
                new CodigoPareamento(
                        deviceId,
                        expiracao
                )
        );


        return codigo;
    }


    // =========================================================
    // PAREAR
    // =========================================================

    public boolean parear(
            String codigo,
            String username
    ) {

        CodigoPareamento pareamento =
                codigos.get(codigo);


        if (pareamento == null) {
            return false;
        }


        if (
                System.currentTimeMillis()
                        > pareamento.expiracao()
        ) {

            codigos.remove(codigo);

            return false;
        }


        Dispositivo dispositivo =
                dispositivos.get(
                        pareamento.deviceId()
                );


        if (dispositivo == null) {

            codigos.remove(codigo);

            return false;
        }


        if (dispositivo.getUsername() != null) {

            return false;
        }


        dispositivo.setUsername(
                username
        );


        codigos.remove(codigo);


        return true;
    }


    // =========================================================
    // RESULTADO DO REGISTRO
    // =========================================================

    public record RegistroDispositivo(
            String deviceId,
            String deviceToken,
            String codigoPareamento
    ) {}


    private record CodigoPareamento(
            String deviceId,
            long expiracao
    ) {}
}
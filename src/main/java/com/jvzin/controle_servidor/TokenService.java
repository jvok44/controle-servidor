package com.jvzin.controle_servidor;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private static final long DURACAO_TOKEN =
            24 * 60 * 60;

    private final SecureRandom random =
            new SecureRandom();

    private final Map<String, Sessao> sessoes =
            new ConcurrentHashMap<>();


    public String criarToken(
            String username
    ) {

        byte[] bytes =
                new byte[32];

        random.nextBytes(bytes);

        String token =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

        long expiracao =
                Instant.now().getEpochSecond()
                        + DURACAO_TOKEN;

        sessoes.put(
                token,
                new Sessao(
                        username,
                        expiracao
                )
        );

        return token;
    }


    public String buscarUsuario(
            String token
    ) {

        Sessao sessao =
                sessoes.get(token);

        if (sessao == null) {
            return null;
        }


        long agora =
                Instant.now().getEpochSecond();


        if (agora >= sessao.expiracao()) {

            sessoes.remove(token);

            return null;
        }


        return sessao.username();
    }


    public void removerToken(
            String token
    ) {

        sessoes.remove(token);
    }


    private record Sessao(
            String username,
            long expiracao
    ) {}
}
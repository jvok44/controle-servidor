package com.jvzin.controle_servidor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class DeviceAuthenticationFilter
        extends OncePerRequestFilter {

    private final DispositivoService dispositivoService;

    public DeviceAuthenticationFilter(
            DispositivoService dispositivoService
    ) {
        this.dispositivoService =
                dispositivoService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");


        if (
                authorization != null &&
                authorization.startsWith("Device ")
        ) {

            String token =
                    authorization.substring(7);


            String deviceId =
                    extrairDeviceId(request);


            if (
                    deviceId != null &&
                    dispositivoService.sessaoDispositivoValida(
                            deviceId,
                            token
                    )
            ) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "DEVICE:" + deviceId,
                                null,
                                Collections.emptyList()
                        );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }
        }


        filterChain.doFilter(
                request,
                response
        );
    }


    private String extrairDeviceId(
            HttpServletRequest request
    ) {

        String uri =
                request.getRequestURI();


        String prefix =
                "/dispositivos/";


        if (!uri.startsWith(prefix)) {
            return null;
        }


        String restante =
                uri.substring(
                        prefix.length()
                );


        int barra =
                restante.indexOf("/");


        if (barra == -1) {
            return null;
        }


        return restante.substring(
                0,
                barra
        );
    }
}
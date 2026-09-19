package com.jvzin.controle_servidor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenService tokenService,
            DispositivoService dispositivoService
    ) throws Exception {

        TokenAuthenticationFilter tokenFilter =
                new TokenAuthenticationFilter(tokenService);

        DeviceAuthenticationFilter deviceFilter =
                new DeviceAuthenticationFilter(
                        dispositivoService
                );

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // ==============================
                        // AUTENTICAÇÃO DA CONTA
                        // ==============================

                        .requestMatchers("/auth/**")
                        .permitAll()


                        // ==============================
                        // REGISTRO DO DISPOSITIVO
                        // ==============================

                        .requestMatchers(
                                "/dispositivos/registrar",
                                "/dispositivos/autenticar"
                        )
                        .permitAll()


                        // Todo o resto precisa
                        // de autenticação
                        .anyRequest()
                        .authenticated()
                )

                // Dispositivo primeiro
                .addFilterBefore(
                        deviceFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // Conta depois
                .addFilterBefore(
                        tokenFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
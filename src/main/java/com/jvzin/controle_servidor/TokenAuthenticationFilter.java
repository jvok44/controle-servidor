package com.jvzin.controle_servidor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class TokenAuthenticationFilter
        extends OncePerRequestFilter {

    private final TokenService tokenService;


    public TokenAuthenticationFilter(
            TokenService tokenService
    ) {

        this.tokenService =
                tokenService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {


        String authorization =
                request.getHeader(
                        "Authorization"
                );


        if (
                authorization != null
                        &&
                authorization.startsWith(
                        "Bearer "
                )
        ) {

            String token =
                    authorization.substring(7);


            String username =
                    tokenService.buscarUsuario(
                            token
                    );


            if (username != null) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.emptyList()
                        );


                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
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
}
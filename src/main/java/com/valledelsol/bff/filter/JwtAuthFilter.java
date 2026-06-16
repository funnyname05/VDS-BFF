package com.valledelsol.bff.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT del BFF.
 *
 * Verifica el token localmente (misma clave secreta que auth-service)
 * y setea el contexto de seguridad con email + rol.
 * Esto evita una llamada HTTP a auth-service en CADA request.
 *
 * Basado en el SecurityFilter original del monolito, adaptado para
 * trabajar sin acceso a UserRepository (solo claims del JWT).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${api.security.token.secret}")
    private String apiSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            var token = authHeader.replace("Bearer ", "");
            try {
                var verifier = JWT.require(Algorithm.HMAC256(apiSecret))
                        .withIssuer("valle_del_sol")
                        .build()
                        .verify(token);

                var email = verifier.getSubject();

                // Más defensivo — evita NullPointerException si el claim no existe
                String rol;
                try {
                    rol = verifier.getClaim("role").asString();
                    if (rol == null || rol.isBlank()) rol = "ROLE_CIVIL";
                } catch (Exception e) {
                    rol = "ROLE_CIVIL";
                }

                System.out.println(">>> JWT válido — email: " + email + " | rol: " + rol);

                var auth = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority(rol))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JWTVerificationException e) {
                System.out.println(">>> JWT inválido: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

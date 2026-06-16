package com.valledelsol.bff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BffProxyController {

    private final WebClient authClient;
    private final WebClient userClient;
    private final WebClient reporteClient;
    private final WebClient alertClient;

    public BffProxyController(
            @Value("${auth.service.url}") String authUrl,
            @Value("${user.service.url}") String userUrl,
            @Value("${reporte.service.url}") String reporteUrl,
            @Value("${alert.service.url}") String alertUrl) {
        this.authClient    = WebClient.builder().baseUrl(authUrl).build();
        this.userClient    = WebClient.builder().baseUrl(userUrl).build();
        this.reporteClient = WebClient.builder().baseUrl(reporteUrl).build();
        this.alertClient   = WebClient.builder().baseUrl(alertUrl).build();
    }

    // ── AUTH ─────────────────────────────────────────────────────────────────

    @PostMapping("/auth/login")
    public Mono<ResponseEntity<String>> login(
            @RequestBody Map<String, Object> body) {
        return authClient.post()
                .uri("/auth/login")
                .bodyValue(body)
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/auth/registro")
    public Mono<ResponseEntity<String>> registro(
            @RequestBody Map<String, Object> body) {
        // Registra en auth-service primero, luego en user-service
        return authClient.post()
                .uri("/auth/registro")
                .bodyValue(body)
                .retrieve()
                .toEntity(String.class)
                .flatMap(authResp -> {
                    if (authResp.getStatusCode().is2xxSuccessful()) {
                        return userClient.post()
                                .uri("/usuarios/registro")
                                .bodyValue(body)
                                .retrieve()
                                .toEntity(String.class);
                    }
                    return Mono.just(authResp);
                });
    }

    // ── USUARIOS ─────────────────────────────────────────────────────────────

    @GetMapping("/usuarios")
    public Mono<ResponseEntity<String>> listarUsuarios(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return userClient.get()
                .uri("/usuarios")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/usuarios/{id}")
    public Mono<ResponseEntity<String>> obtenerUsuario(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return userClient.get()
                .uri("/usuarios/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .toEntity(String.class);
    }

    @PutMapping("/admin/usuarios/{id}/rol")
    public Mono<ResponseEntity<String>> cambiarRol(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return userClient.put()
                .uri("/admin/usuarios/{id}/rol", id)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .bodyValue(body)
                .retrieve()
                .toEntity(String.class);
    }

    // ── REPORTES ──────────────────────────────────────────────────────────────

    @GetMapping("/reportes")
    public Mono<ResponseEntity<String>> listarReportes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return reporteClient.get()
                .uri("/reportes")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/reportes/{id}")
    public Mono<ResponseEntity<String>> obtenerReporte(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return reporteClient.get()
                .uri("/reportes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/reportes")
    public Mono<ResponseEntity<String>> crearReporte(
            @RequestBody Map<String, Object> body,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return reporteClient.post()
                .uri("/reportes")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .bodyValue(body)
                .retrieve()
                .toEntity(String.class);
    }

    @PutMapping("/reportes/{id}/estado")
    public Mono<ResponseEntity<String>> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return reporteClient.put()
                .uri("/reportes/{id}/estado", id)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .bodyValue(body)
                .retrieve()
                .toEntity(String.class);
    }

    @DeleteMapping("/reportes/{id}")
    public Mono<ResponseEntity<String>> eliminarReporte(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return reporteClient.delete()
                .uri("/reportes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .toEntity(String.class);
    }

    // ── ALERTAS ───────────────────────────────────────────────────────────────

    @GetMapping("/alertas")
    public Mono<ResponseEntity<String>> listarAlertas(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return alertClient.get()
                .uri("/alertas")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/alertas")
    public Mono<ResponseEntity<String>> crearAlerta(
            @RequestBody Map<String, Object> body,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return alertClient.post()
                .uri("/alertas")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .bodyValue(body)
                .retrieve()
                .toEntity(String.class);
    }

    @DeleteMapping("/alertas/{id}")
    public Mono<ResponseEntity<String>> eliminarAlerta(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return alertClient.delete()
                .uri("/alertas/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .toEntity(String.class);
    }
}
package grit.sistema.backend.controller;

import grit.sistema.backend.dto.*;
import grit.sistema.backend.service.AtletaService;
import grit.sistema.backend.service.JwtService;
import grit.sistema.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UsuarioService usuarioService;
    private final AtletaService atletaService;
    private final JwtService jwtService;

    @PostMapping("/registro/atleta")
    public ResponseEntity<AtletaResponseDTO> registrarAtleta(@Valid @RequestBody AtletaRequestDTO dto) {
        log.info(">>> Solicitud de registro de atleta recibida: {}", dto.email());

        AtletaResponseDTO respuesta = atletaService.registrarAtleta(dto);

        String accessToken = jwtService.generarAccessToken(dto.email(), "ATLETA");
        String refreshToken = jwtService.generarRefreshToken(dto.email());

        HttpHeaders headers = generarCookiesHeaders(accessToken, refreshToken);

        log.info("<<< Atleta registrado y tokens emitidos en cookies para: {}", dto.email());

        return new ResponseEntity<>(respuesta, headers, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDto) {
        log.info(">>> Solicitud de login recibida para el email: {}", loginDto.email());

        AuthResponseDTO response = usuarioService.login(loginDto);

        // Para cumplir con el requerimiento de cookies en el login:
        // Suponiendo que 'response' tiene los tokens que generó el usuarioService
        String accessToken = jwtService.generarAccessToken(loginDto.email(), "DEBE DE IR EL ROL");
        String refreshToken = jwtService.generarRefreshToken(loginDto.email());

        HttpHeaders headers = generarCookiesHeaders(accessToken, refreshToken);

        // Log de éxito
        log.info("<<< Login exitoso para el usuario con UUID: {}", response.usuario().idPublico());
        return ResponseEntity.ok().headers(headers).body(response);
    }

    // --- MÉTODOS DE APOYO PRIVADOS ---

    private HttpHeaders generarCookiesHeaders(String access, String refresh) {
        ResponseCookie accessCookie = construirCookie("access_token", access, 900, "/");
        ResponseCookie refreshCookie = construirCookie("refresh_token", refresh, 604800, "/api/v1/auth/refresh");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    private ResponseCookie construirCookie(String nombre, String valor, long maxAge, String path) {
        return ResponseCookie.from(nombre, valor)
                .httpOnly(true)
                .secure(true) // Importante: Solo viaja por HTTPS
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}

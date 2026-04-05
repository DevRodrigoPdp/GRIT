package grit.sistema.backend.controller;

import grit.sistema.backend.dto.*;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.service.AtletaService;
import grit.sistema.backend.service.JwtService;
import grit.sistema.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UsuarioService usuarioService;
    private final AtletaService atletaService;
    private final JwtService jwtService;

    // Inyectamos las mismas variables que en JwtService
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @PostMapping("/registro/atleta")
    public ResponseEntity<AtletaResponseDTO> registrarAtleta(@Valid @RequestBody AtletaRequestDTO dto) {
        log.info(">>> Solicitud de registro de atleta recibida: {}", dto.email());

        AtletaResponseDTO respuesta = atletaService.registrarAtleta(dto);

        String accessToken = jwtService.generarAccessToken(dto.email(), Rol.ATLETA.name());
        String refreshToken = jwtService.generarRefreshToken(dto.email());

        HttpHeaders headers = generarCookiesHeaders(accessToken, refreshToken);

        log.info("<<< Atleta registrado y tokens emitidos en cookies para: {}", dto.email());

        return new ResponseEntity<>(respuesta, headers, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDto) {
        log.info(">>> Solicitud de login recibida para el email: {}", loginDto.email());

        LoginResponseDTO response = usuarioService.login(loginDto);

        // Para cumplir con el requerimiento de cookies en el login:
        // Suponiendo que 'response' tiene los tokens que generó el usuarioService
        String refreshToken = jwtService.generarRefreshToken(loginDto.email());
        String accessToken = jwtService.generarAccessToken(loginDto.email(), response.data().rol());

        HttpHeaders headers = generarCookiesHeaders(accessToken, refreshToken);

        // Log de éxito
        log.info("<<< Login exitoso para el usuario con email: {}", loginDto.email());
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info(">>> Solicitud de cierre de sesión");

        // Para cerrar sesión, enviamos cookies vacías con tiempo de vida 0
        ResponseCookie accessCookie = construirCookie("access_token", "", 0, "/");
        ResponseCookie refreshCookie = construirCookie("refresh_token", "", 0, "/api/v1/auth/refresh");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        log.info(">>> Solicitud de refresco de token recibida");

        if (refreshToken == null || !jwtService.esTokenValido(refreshToken, jwtService.extraerEmail(refreshToken))) {
            log.warn("Refresh token ausente o inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 1. Extraer datos del token actual
        String email = jwtService.extraerEmail(refreshToken);

        // 2. Obtener los datos del usuario para el nuevo Access Token y el Body
        // Usamos el servicio para asegurar que el usuario sigue activo y con el mismo rol
        UsuarioDTO usuarioDto = usuarioService.findByEmail(email);
        // Nota: Asegúrate de tener findByEmail en tu Service que devuelva los datos necesarios

        // 3. Generar nuevo Access Token
        String newAccessToken = jwtService.generarAccessToken(email, usuarioDto.rol());

        // 4. Generar las headers (el Refresh Token se mantiene o se puede rotar)
        // En este caso, reutilizamos el mismo Refresh para no cerrar sesión al usuario
        HttpHeaders headers = generarCookiesHeaders(newAccessToken, refreshToken);

        // 5. Construir respuesta (Reutilizamos LoginData para que el frontend actualice su estado)
        // Necesitarás un pequeño ajuste en tu mapper para esto
        LoginData data = usuarioService.obtenerDatosParaRefresh(email);
        LoginResponseDTO response = new LoginResponseDTO(true, data);

        log.info("<<< Token refrescado exitosamente para: {}", email);
        return ResponseEntity.ok().headers(headers).body(response);
    }

    // --- MÉTODOS DE APOYO PRIVADOS ---

    private HttpHeaders generarCookiesHeaders(String access, String refresh) {
        // Convertimos milisegundos a segundos para la cookie
        long accessSeconds = jwtExpiration / 1000;
        long refreshSeconds = refreshExpiration / 1000;

        ResponseCookie accessCookie = construirCookie("access_token", access, accessSeconds, "/");
        ResponseCookie refreshCookie = construirCookie("refresh_token", refresh, refreshSeconds, "/api/v1/auth/refresh");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    private ResponseCookie construirCookie(String nombre, String valor, long maxAge, String path) {
        return ResponseCookie.from(nombre, valor)
                .httpOnly(true)
                .secure(false) // Importante: Solo viaja por HTTPS para desarrollo dejarlo en false
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}

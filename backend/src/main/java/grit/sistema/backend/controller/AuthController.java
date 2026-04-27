package grit.sistema.backend.controller;

import grit.sistema.backend.dto.coaching.AtletaRequestDTO;
import grit.sistema.backend.dto.coaching.AtletaResponseDTO;
import grit.sistema.backend.dto.usuario.MeResponseDTO;
import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.dto.coaching.EntrenadorResponseDTO;
import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.auth.LoginRequestDTO;
import grit.sistema.backend.dto.auth.LoginResponseDTO;
import grit.sistema.backend.dto.usuario.UsuarioDTO;
import grit.sistema.backend.exception.SesionActivaException;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.service.coaching.AtletaService;
import grit.sistema.backend.service.coaching.EntrenadorService;
import grit.sistema.backend.security.JwtUtils;
import grit.sistema.backend.service.auth.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UsuarioService usuarioService;
    private final AtletaService atletaService;
    private final EntrenadorService entrenadorService;
    private final JwtUtils jwtUtils;

    // Inyectamos las mismas variables que en JwtService
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.cookie.secure}")
    private boolean isSecure;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Operation(
            summary = "Registro de Entrenador",
            description = "Registra un nuevo entrenador. Requiere datos personales y archivos de titulación (Multipart). El estado inicial será PENDIENTE_REVISION."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitud recibida correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o falta de documentos",content = @Content),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado",content = @Content)
    })
    @PostMapping(value = "/registro/entrenador", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<EntrenadorResponseDTO>> registrarEntrenador(
            @RequestPart("datos") @Valid EntrenadorRequestDTO dto,
            @RequestPart(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
            @RequestPart("certificaciones") List<MultipartFile> certificaciones) {
        log.info(">>> Solicitud de registro de ENTRENADOR recibida: {}", dto.getEmail());

        EntrenadorResponseDTO data = entrenadorService.registrarEntrenador(dto, fotoPerfil, certificaciones);

        ApiResponseDTO<EntrenadorResponseDTO> respuesta = ApiResponseDTO.success(
                data,
                "Solicitud recibida. Revisaremos tus credenciales en un plazo máximo de 48h y te notificaremos por correo."
        );

        log.info("<<< Entrenador registrado exitosamente en estado PENDIENTE: {}", dto.getEmail());

        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Registro de Atleta",
            description = "Crea una cuenta de atleta y emite automáticamente cookies de sesión (Access y Refresh Token)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Atleta creado y sesión iniciada"),
            @ApiResponse(responseCode = "400", description = "Error en los datos de validación",content = @Content)
    })
    @PostMapping(value ="/registro/atleta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AtletaResponseDTO> registrarAtleta(
            @RequestPart("datos") @Valid AtletaRequestDTO dto,
            @RequestPart(value = "fotoPerfil", required = false) MultipartFile foto) {
        log.info(">>> Solicitud de registro de atleta recibida: {}", dto.email());

        AtletaResponseDTO respuesta = atletaService.registrarAtleta(dto, foto);

        String accessToken = jwtUtils.generarAccessToken(dto.email(), Rol.ATLETA.name());
        String refreshToken = jwtUtils.generarRefreshToken(dto.email());

        HttpHeaders headers = generarCookiesHeaders(accessToken, refreshToken);

        log.info("<<< Atleta registrado y tokens emitidos en cookies para: {}", dto.email());

        return new ResponseEntity<>(respuesta, headers, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Login de Usuario",
            description = "Autentica al usuario y establece las cookies HttpOnly 'access_token' y 'refresh_token'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDto) {
        log.info(">>> Solicitud de login recibida para el email: {}", loginDto.email());

        LoginResponseDTO response = usuarioService.login(loginDto);

        // Para cumplir con el requerimiento de cookies en el login:
        // Suponiendo que 'response' tiene los tokens que generó el usuarioService
        String refreshToken = jwtUtils.generarRefreshToken(loginDto.email());
        String accessToken = jwtUtils.generarAccessToken(loginDto.email(), response.data().rol());

        HttpHeaders headers = generarCookiesHeaders(accessToken, refreshToken);

        // Log de éxito
        log.info("<<< Login exitoso para el usuario con email: {}", loginDto.email());
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @Operation(
            summary = "Logout",
            description = "Limpia las cookies de sesión del navegador estableciendo su tiempo de vida a cero."
    )
    @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente")
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

    @Operation(
            summary = "Refrescar Access Token",
            description = "Utiliza la cookie 'refresh_token' para emitir un nuevo 'access_token' sin pedir credenciales."
    )

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado",content = @Content)
    })

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @Parameter(hidden = true)
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        log.info(">>> Solicitud de refresco de token recibida");

        if (refreshToken == null || !jwtUtils.esTokenValido(refreshToken, jwtUtils.extraerEmail(refreshToken))) {
            log.warn("Refresh token ausente o inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 1. Extraer datos del token actual
        String email = jwtUtils.extraerEmail(refreshToken);

        // 2. Obtener los datos del usuario para el nuevo Access Token y el Body
        // Usamos el servicio para asegurar que el usuario sigue activo y con el mismo rol
        UsuarioDTO usuarioDto = usuarioService.findByEmail(email);
        // Nota: Asegúrate de tener findByEmail en tu Service que devuelva los datos necesarios

        // 3. Generar nuevo Access Token
        String newAccessToken = jwtUtils.generarAccessToken(email, usuarioDto.rol());

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

    @Operation(
            summary = "Devuelve los datos del usuario a través del access_token",
            description = "Obtiene los datos del usuario autenticado a partir del access_token en la cookie."
    )
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("Intento de acceso a /me sin autenticación válida");
            throw new SesionActivaException("Intento de acceso a /me inválido");
        }

        log.info(">>> Restaurando sesión para: {}", userDetails.getUsername());

        MeResponseDTO response = usuarioService.obtenerMiInformacion(userDetails.getUsername());

        return ResponseEntity.ok(response);
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
                .secure(isSecure) // Importante: Solo viaja por HTTPS para desarrollo dejarlo en false
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}

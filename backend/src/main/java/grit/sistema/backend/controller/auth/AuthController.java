package grit.sistema.backend.controller.auth;

import grit.sistema.backend.dto.coaching.AtletaRequestDTO;
import grit.sistema.backend.dto.coaching.AtletaResponseDTO;
import grit.sistema.backend.dto.user.MeResponseDTO;
import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.dto.coaching.EntrenadorResponseDTO;
import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.auth.LoginRequestDTO;
import grit.sistema.backend.dto.auth.LoginResponseDTO;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.security.jwt.TokenStoreService;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.auth.AuthService;
import grit.sistema.backend.service.coaching.AtletaService;
import grit.sistema.backend.service.coaching.EntrenadorService;
import grit.sistema.backend.security.jwt.JwtUtils;
import grit.sistema.backend.service.user.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final AtletaService atletaService;
    private final EntrenadorService entrenadorService;
    private final JwtUtils jwtUtils;
    private final TokenStoreService tokenStoreService;

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
        EntrenadorResponseDTO data = entrenadorService.registrarEntrenador(dto, fotoPerfil, certificaciones);

        ApiResponseDTO<EntrenadorResponseDTO> respuesta = ApiResponseDTO.success(
                data,
                "Solicitud recibida. Revisaremos tus credenciales en un plazo máximo de 48h y te notificaremos por correo."
        );

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

        AtletaResponseDTO respuesta = atletaService.registrarAtleta(dto, foto);

        String refreshJti = UUID.randomUUID().toString();

        ResponseCookie accessCookie = jwtUtils.generateAccessCookie(dto.email(), Rol.ATLETA.name(), refreshJti);
        ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(dto.email(), refreshJti);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(respuesta);
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

        LoginResponseDTO response = authService.login(loginDto);

        String refreshJti = UUID.randomUUID().toString();

        ResponseCookie accessCookie = jwtUtils.generateAccessCookie(loginDto.email(), response.data().rol(), refreshJti);
        ResponseCookie refreshCookie = jwtUtils.generateRefreshCookie(loginDto.email(), refreshJti);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @Operation(
            summary = "Logout",
            description = "Limpia las cookies de sesión del navegador estableciendo su tiempo de vida a cero."
    )
    @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {

        ResponseCookie accessCookie = jwtUtils.getCleanAccessCookie();
        ResponseCookie refreshCookie = jwtUtils.getCleanRefreshCookie();

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

        if (refreshToken == null || !jwtUtils.esTokenValido(refreshToken, jwtUtils.extraerEmail(refreshToken))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String jti = jwtUtils.extraerJti(refreshToken);
        String email = jwtUtils.extraerEmail(refreshToken);

        if (tokenStoreService.isReuseDetected(jti)) {
            log.error("¡ALERTA! Reúso de JTI detectado: {} para el usuario {}", jti, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LoginData data = usuarioService.obtenerDatosParaRefresh(email);
        tokenStoreService.burnJti(jti);

        String refreshJti = UUID.randomUUID().toString();

        ResponseCookie newAccessCookie = jwtUtils.generateAccessCookie(email, data.rol(), refreshJti);
        ResponseCookie newRefreshCookie = jwtUtils.generateRefreshCookie(email,  refreshJti);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(new LoginResponseDTO(true, data));
    }

    @Operation(
            summary = "Devuelve los datos del usuario a través del access_token",
            description = "Obtiene los datos del usuario autenticado a partir del access_token en la cookie."
    )
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> getCurrentUser(@AuthenticationPrincipal UserPrincipal usuario) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MeResponseDTO response = usuarioService.obtenerMiInformacion(usuario.getId());

        return ResponseEntity.ok(response);
    }
}

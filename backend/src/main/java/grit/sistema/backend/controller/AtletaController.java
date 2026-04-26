package grit.sistema.backend.controller;

import grit.sistema.backend.dto.ApiResponseDTO;
import grit.sistema.backend.dto.atleta.ProfesionalAsignadoDTO;
import grit.sistema.backend.dto.atleta.AtletaPerfilDTO;
import grit.sistema.backend.dto.atleta.VinculacionRequestDTO;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.training.*;
import grit.sistema.backend.security.UserPrincipal;
import grit.sistema.backend.service.AtletaService;
import grit.sistema.backend.service.PesoService;
import grit.sistema.backend.service.VinculacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/atleta")
@Tag(name = "Atletas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ATLETA')")
@Slf4j
public class AtletaController {
    private final AtletaService atletaService;
    private final VinculacionService vinculacionService;
    private final PesoService pesoService;

    @Operation(summary = "Ver perfil del atleta")
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<AtletaPerfilDTO>> getPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Consultando perfil para el atleta: {}", userDetails.getUsername());
        AtletaPerfilDTO perfilDTO = atletaService.obtenerPerfil(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Perfil del atleta", perfilDTO));
    }

    @Operation(summary = "Ver plan activo del atleta")
    @GetMapping("/entrenamiento/plan-activo")
    public ResponseEntity<ApiResponseDTO<RutinaDTO>> getPlanActivo(@AuthenticationPrincipal UserPrincipal usuario) {
        log.info("Consultando plan activo para el atleta: {}", usuario.getUsername());
        return atletaService.getPlanActivoAtleta(usuario.getId())
                .map(plan -> ResponseEntity.ok(new ApiResponseDTO<>(true, "Plan activo", plan)))
                .orElseGet(() -> ResponseEntity.ok(new ApiResponseDTO<>(true, "No hay plan activo", null)));
    }

    @Operation(summary = "Ver profesionales asignados a un atleta")
    @GetMapping("/profesionales")
    public ResponseEntity<ApiResponseDTO<List<ProfesionalAsignadoDTO>>> getProfesionalesAsignados(@AuthenticationPrincipal UserPrincipal usuario) {
        log.info("Consultando profesionales del atleta: {}", usuario.getUsername());
        List<ProfesionalAsignadoDTO> profesionales = atletaService.getProfesionalesAsignados(usuario.getId());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Profesionales del atleta", profesionales));
    }

    @Operation(summary = "Conexión con el entrenador por código de invitación en el perfil")
    @PostMapping("/conectar")
    public ResponseEntity<ApiResponseDTO> conectarConEntrenador(
            @Valid @RequestBody VinculacionRequestDTO request,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        vinculacionService.conectarConEntrenador(usuario.getId(), request.codigo());

        return ResponseEntity.ok(new ApiResponseDTO(
                true,
                "Vinculado correctamente con el entrenador.",
                null
        ));
    }

    @GetMapping("/peso/solicitud-pendiente")
    public ResponseEntity<ApiResponseDTO<SolicitudPendienteDTO>> getPendiente(@AuthenticationPrincipal UserPrincipal usuario) {
        var data = pesoService.obtenerSolicitudPendiente(usuario.getId());
        return ResponseEntity.ok(new ApiResponseDTO<>(true,"Solicitud recuperada", data));
    }

    @PostMapping("/peso")
    public ResponseEntity<ApiResponseDTO<PesoResponseDTO>> registrar(@Valid @RequestBody PesoRequestDTO dto, @AuthenticationPrincipal UserPrincipal usuario) {
        var data = pesoService.registrarPeso(dto, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, "Peso registrado con éxito",data));
    }

    @GetMapping("peso/historial")
    public ResponseEntity<ApiResponseDTO<List<HistorialPesoDTO>>> getHistorial(@AuthenticationPrincipal UserPrincipal usuario) {
        var data = pesoService.obtenerHistorialAtleta(usuario.getId());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Historial recuperado", data));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto, @AuthenticationPrincipal UserPrincipal usuario) {
        atletaService.cambiarPassword(usuario.getEmail(), dto);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/cuenta")
    public ResponseEntity<ApiResponseDTO> eliminarCuenta(
            @AuthenticationPrincipal UserPrincipal usuario,
            HttpServletResponse response) {

        atletaService.solicitarBajaCuenta(usuario.getId());

        // Invalidar Cookie
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new ApiResponseDTO(true, "Tu cuenta de atleta ha sido desactivada.", null));
    }
}

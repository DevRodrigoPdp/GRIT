package grit.sistema.backend.controller.coaching;

import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.coaching.ProfesionalAsignadoDTO;
import grit.sistema.backend.dto.coaching.AtletaPerfilDTO;
import grit.sistema.backend.dto.coaching.AsignacionRequestDTO;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.nutrition.NotaNutricionistaRequestDTO;
import grit.sistema.backend.dto.nutrition.NotaResponseDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionActivoResponseDTO;
import grit.sistema.backend.dto.training.*;
import grit.sistema.backend.security.user.UserPrincipal;
import grit.sistema.backend.service.coaching.AtletaService;
import grit.sistema.backend.service.nutrition.NutricionService;
import grit.sistema.backend.service.training.EntrenamientoService;
import grit.sistema.backend.service.training.PesoService;
import grit.sistema.backend.service.coaching.AsignacionService;
import grit.sistema.backend.service.user.UsuarioService;
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
    private final UsuarioService usuarioService;
    private final AtletaService atletaService;
    private final NutricionService nutricionService;
    private final EntrenamientoService entrenamientoService;
    private final AsignacionService asignacionService;
    private final PesoService pesoService;

    @Operation(summary = "Ver perfil del atleta")
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<AtletaPerfilDTO>> getPerfil(@AuthenticationPrincipal UserPrincipal usuario) {
        AtletaPerfilDTO perfilDTO = atletaService.obtenerPerfil(usuario.getId());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Perfil del atleta", perfilDTO));
    }

    @Operation(summary = "Ver plan activo entrenamiento del atleta")
    @GetMapping("/entrenamiento/plan-activo")
    public ResponseEntity<ApiResponseDTO<RutinaDTO>> getPlanEntrenamientoActivo(@AuthenticationPrincipal UserPrincipal usuario) {
        return entrenamientoService.getPlanEntrenamientoActivoAtleta(usuario.getId())
                .map(plan -> ResponseEntity.ok(new ApiResponseDTO<>(true, "Plan entrenamiento activo", plan)))
                .orElseGet(() -> ResponseEntity.ok(new ApiResponseDTO<>(true, "No hay plan entrenamiento activo", null)));
    }

    @Operation(summary = "Ver plan activo nutrición del atleta")
    @GetMapping("/nutricion/plan-activo")
    public ResponseEntity<PlanNutricionActivoResponseDTO> getPlanNutricionActivo(@AuthenticationPrincipal UserPrincipal usuario) {

        PlanNutricionActivoResponseDTO response = nutricionService.getPlanNutricionActivoAtleta(usuario.getId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ver profesionales asignados a un atleta")
    @GetMapping("/profesionales")
    public ResponseEntity<ApiResponseDTO<List<ProfesionalAsignadoDTO>>> getProfesionalesAsignados(@AuthenticationPrincipal UserPrincipal usuario) {
        List<ProfesionalAsignadoDTO> profesionales = atletaService.getProfesionalesAsignados(usuario.getId());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Profesionales del atleta", profesionales));
    }

    @Operation(summary = "Conexión con el entrenador por código de invitación en el perfil")
    @PostMapping("/conectar")
    public ResponseEntity<Map<String, Object>> conectarConEntrenador(
            @Valid @RequestBody AsignacionRequestDTO request,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        asignacionService.conectarConEntrenador(usuario.getId(), request.codigo());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "ok", true,
                "mensaje", "EL atleta ha conectado con el entrenador correctamente"
        ));
    }

    @GetMapping("/nutricion/notas")
    public ResponseEntity<Map<String, Object>> verMisNotas(@AuthenticationPrincipal UserPrincipal usuario) {
        List<NotaResponseDTO> notas = nutricionService.obtenerNotasAtleta(usuario.getId());

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "data", notas
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
        usuarioService.actualizarPassword(usuario.getId(), dto);
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

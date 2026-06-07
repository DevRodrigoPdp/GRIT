package grit.sistema.backend.controller.coaching;

import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.coaching.*;
import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionActivoResponseDTO;
import grit.sistema.backend.dto.training.*;
import grit.sistema.backend.security.jwt.JwtUtils;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.coaching.AsignacionService;
import grit.sistema.backend.service.coaching.AtletaService;
import grit.sistema.backend.service.nutrition.NutricionService;
import grit.sistema.backend.service.training.EntrenamientoService;
import grit.sistema.backend.service.training.PesoService;
import grit.sistema.backend.service.user.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private final JwtUtils jwtUtils;

    @Operation(summary = "Ver perfil del atleta")
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<AtletaPerfilDTO>> getPerfil(@AuthenticationPrincipal UserPrincipal usuario) {
        AtletaPerfilDTO perfilDTO = atletaService.obtenerPerfil(usuario.getId());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache()
                        .cachePrivate()
                        .mustRevalidate())
                .body(new ApiResponseDTO<>(true, "Perfil del atleta", perfilDTO));
    }

    @Operation(summary = "Actualizar perfil de atleta")
    @PutMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<AtletaPerfilDTO>> actualizarPerfil(@Valid @RequestBody AtletaEditarPerfilDTO dto, @AuthenticationPrincipal UserPrincipal usuario) {
        AtletaPerfilDTO perfilDTO = atletaService.editarPerfil(usuario.getId(), dto);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Perfil del atleta actualizado", perfilDTO));
    }

    @Operation(summary = "Actualizar foto de perfil")
    @PatchMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<FotoPerfilResponseDTO>> actualizarFoto(
            @RequestPart("fotoPerfil") MultipartFile foto,
            @AuthenticationPrincipal UserPrincipal usuario) {
        FotoPerfilResponseDTO response = usuarioService.actualizarFotoPerfil(usuario.getId(), foto);

        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Foto actualizada exitosamente", response));
    }

    @Operation(summary = "Ver plan activo entrenamiento del atleta")
    @GetMapping("/entrenamiento/plan-activo")
    public ResponseEntity<ApiResponseDTO<RutinaDTO>> getPlanEntrenamientoActivo(@AuthenticationPrincipal UserPrincipal usuario) {
        return entrenamientoService.getPlanEntrenamientoActivoAtleta(usuario.getId())
                .map(plan ->
                        ResponseEntity.ok()
                                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate().mustRevalidate())
                                .body(new ApiResponseDTO<>(true, "Plan entrenamiento activo", plan)))
                .orElseGet(() ->
                        ResponseEntity.ok()
                                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate().mustRevalidate())
                                .body(new ApiResponseDTO<>(true, "No hay plan entrenamiento activo", null)));
    }

    @Operation(summary = "Ver plan activo nutrición del atleta")
    @GetMapping("/nutricion/plan-activo")
    public ResponseEntity<PlanNutricionActivoResponseDTO> getPlanNutricionActivo(@AuthenticationPrincipal UserPrincipal usuario) {

        PlanNutricionActivoResponseDTO response = nutricionService.getPlanNutricionActivoAtleta(usuario.getId());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate().mustRevalidate())
                .body(response);
    }

    @Operation(summary = "Ver profesionales asignados a un atleta")
    @GetMapping("/profesionales")
    public ResponseEntity<ApiResponseDTO<List<ProfesionalAsignadoDTO>>> getProfesionalesAsignados(@AuthenticationPrincipal UserPrincipal usuario) {
        List<ProfesionalAsignadoDTO> profesionales = atletaService.getProfesionalesAsignados(usuario.getId());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().cachePrivate().mustRevalidate())
                .body(new ApiResponseDTO<>(true, "Profesionales del atleta", profesionales));
    }

    @Operation(summary = "Darse de baja del entrenador")
    @DeleteMapping("/profesionales/{profesionalId}")
    public ResponseEntity<ApiResponseDTO<Void>> desconectarAtleta(@PathVariable UUID profesionalId, @AuthenticationPrincipal UserPrincipal usuario) {
        asignacionService.terminarAsignacion(profesionalId, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Conexión con el entrenador por código de invitación en el perfil")
    @PostMapping("/conectar")
    public ResponseEntity<ApiResponseDTO<Void>> conectarConEntrenador(
            @Valid @RequestBody AsignacionRequestDTO request,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        asignacionService.conectarConEntrenador(usuario.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Conexión establecida correctamente", null));
    }

    @GetMapping("/peso/solicitud-pendiente")
    public ResponseEntity<ApiResponseDTO<SolicitudPendienteDTO>> getPendiente(@AuthenticationPrincipal UserPrincipal usuario) {
        var data = pesoService.obtenerSolicitudPendiente(usuario.getId());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate().mustRevalidate())
                .body(new ApiResponseDTO<>(true, "Solicitud recuperada", data));
    }

    @PostMapping("/peso")
    public ResponseEntity<ApiResponseDTO<PesoResponseDTO>> registrar(@Valid @RequestBody PesoRequestDTO dto, @AuthenticationPrincipal UserPrincipal usuario) {
        var data = pesoService.registrarPeso(dto, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, "Peso registrado con éxito", data));
    }

    @GetMapping("/peso/historial")
    public ResponseEntity<ApiResponseDTO<List<HistorialPesoDTO>>> getHistorial(@AuthenticationPrincipal UserPrincipal usuario) {
        var data = pesoService.obtenerHistorialAtleta(usuario.getId());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate().mustRevalidate())
                .body(new ApiResponseDTO<>(true, "Historial recuperado", data));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto, @AuthenticationPrincipal UserPrincipal usuario) {
        usuarioService.actualizarPassword(usuario.getId(), dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cuenta")
    public ResponseEntity<Void> eliminarCuenta(@AuthenticationPrincipal UserPrincipal usuario) {

        atletaService.solicitarBajaCuenta(usuario.getId());

        ResponseCookie accessCookie = jwtUtils.getCleanAccessCookie();
        ResponseCookie refreshCookie = jwtUtils.getCleanRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }
}

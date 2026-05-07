package grit.sistema.backend.controller.coaching;

import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.coaching.AtletaResumenDTO;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.coaching.EntrenadorPerfilDTO;
import grit.sistema.backend.dto.nutrition.NotaNutricionistaRequestDTO;
import grit.sistema.backend.dto.nutrition.NotaResponseDTO;
import grit.sistema.backend.dto.training.HistorialPesoDTO;
import grit.sistema.backend.security.user.UserPrincipal;
import grit.sistema.backend.service.coaching.EntrenadorService;
import grit.sistema.backend.service.nutrition.NutricionService;
import grit.sistema.backend.service.training.PesoService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entrenador")
@Tag(name = "Entrenadores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTRENADOR')")
@Slf4j
public class EntrenadorController {
    private final EntrenadorService entrenadorService;
    private final NutricionService nutricionService;
    private final PesoService pesoService;
    private final UsuarioService usuarioService;

    @Operation(summary = "Ver perfil de entrenador")
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponseDTO<EntrenadorPerfilDTO>> getPerfil(@AuthenticationPrincipal UserPrincipal usuario) {
        EntrenadorPerfilDTO perfilDTO = entrenadorService.obtenerPerfil(usuario.getId());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Perfil del entrenador", perfilDTO));
    }

    @Operation(summary = "Listar atletas asociados a un entrenador")
    @GetMapping("/atletas")
    public ResponseEntity<ApiResponseDTO<List<AtletaResumenDTO>>> getAtletas(@AuthenticationPrincipal UserPrincipal usuario) {
        List<AtletaResumenDTO> listaAtletas = entrenadorService.listarMisAtletas(usuario.getEmail());

        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Atletas del entrenador", listaAtletas));
    }

    @Operation(summary = "Crear nota al atleta")
    @PostMapping("/atletas/{atletaId}/notas")
    public ResponseEntity<Map<String, Object>> dejarNota(
            @PathVariable UUID atletaId,
            @Valid @RequestBody NotaNutricionistaRequestDTO request,
            @AuthenticationPrincipal UserPrincipal usuario) {

        NotaResponseDTO data = nutricionService.crearNota(usuario.getId(), atletaId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "ok", true,
                "data", data
        ));
    }

    @PostMapping("/atletas/{atletaId}/peso/solicitar")
    public ResponseEntity<ApiResponseDTO<String>> solicitar(@PathVariable UUID atletaId, @AuthenticationPrincipal UserPrincipal usuario) {
        pesoService.solicitarCheckin(atletaId, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Solicitud creada correctamente",null));
    }

    @GetMapping("/atletas/{atletaId}/peso/pendiente")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> checkPendiente(@PathVariable UUID atletaId) {
        boolean pendiente = pesoService.tieneSolicitudPendiente(atletaId);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "pendiente", Map.of("pendiente", pendiente)));
    }

    @GetMapping("/atletas/{atletaId}/peso/historial")
    public ResponseEntity<ApiResponseDTO<List<HistorialPesoDTO>>> getHistorialAtleta(@PathVariable UUID atletaId) {
        var data = pesoService.obtenerHistorialAtleta(atletaId);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Historial del atleta recuperado", data));
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

        entrenadorService.solicitarBajaCuenta(usuario.getId());

        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new ApiResponseDTO(true, "Cuenta desactivada correctamente.",null));
    }
}

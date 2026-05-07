package grit.sistema.backend.controller.training;

import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.training.RutinaDTO;
import grit.sistema.backend.dto.training.RutinaRequestDTO;
import grit.sistema.backend.dto.training.RutinaResponseDTO;
import grit.sistema.backend.security.user.UserPrincipal;
import grit.sistema.backend.service.training.EntrenamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entrenamiento")
@Tag(name = "Entrenamiento")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTRENADOR') and @auth.tieneTituloEntrenamiento()")
@Slf4j
public class EntrenamientoController {
    private final EntrenamientoService entrenamientoService;

    @PatchMapping("/rutinas/{rutinaId}/activar")
    public ResponseEntity<Void> activarRutina(
            @PathVariable UUID rutinaId,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        entrenamientoService.activarRutina(usuario.getId(), rutinaId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar rutinas de entrenamiento")
    @GetMapping("/rutinas")
    public ResponseEntity<ApiResponseDTO<List<RutinaDTO>>> listarRutinas(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID atletaId
    ) {
        List<RutinaDTO> rutinas = entrenamientoService.listarRutinas(principal.getId(), atletaId);
        return ResponseEntity.ok(ApiResponseDTO.success(rutinas, "Rutinas encontradas"));
    }

    @Operation(summary = "Crear rutina de entrenamiento")
    @PostMapping("/rutinas")
    public ResponseEntity<ApiResponseDTO<RutinaResponseDTO>> crearRutina(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RutinaRequestDTO request
    ) {
        RutinaResponseDTO response = entrenamientoService.crearRutina(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(response, "Rutina creada correctamente"));
    }

    @Operation(summary = "Eliminar rutina de entrenamiento")
    @DeleteMapping("/rutinas/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> eliminarRutina(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        entrenamientoService.eliminarRutina(principal.getId(), id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Rutina eliminada correctamente"));
    }
}

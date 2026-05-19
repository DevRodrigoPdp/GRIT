package grit.sistema.backend.controller.nutrition;

import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionRequestDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionResponseDTO;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.nutrition.NutricionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/nutricion")
@Tag(name = "Nutrición")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTRENADOR') and @auth.tieneTituloNutricion()")
@Slf4j
public class NutricionController {
    private final NutricionService nutricionService;

    @Operation(summary = "Listar planes de nutrición")
    @PreAuthorize("hasRole('ENTRENADOR') and @asignacionService.esEntrenadorDeAtleta(authentication.principal.id, #atletaId)")
    @GetMapping("/planes")
    public ResponseEntity<ApiResponseDTO<List<PlanNutricionResponseDTO>>> listarPlanes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID atletaId
    ) {
        List<PlanNutricionResponseDTO> planes = nutricionService.listarPlanes(principal.getId(), atletaId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate().mustRevalidate())
                .body(ApiResponseDTO.success(planes, "Planes de nutrición encontrados"));
    }

    @Operation(summary = "Crear plan de nutrición")
    @PostMapping("/planes")
    public ResponseEntity<ApiResponseDTO<PlanNutricionResponseDTO>> crearPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PlanNutricionRequestDTO request
    ) {
        PlanNutricionResponseDTO response = nutricionService.crearPlan(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(response, "Plan de nutrición creado correctamente"));
    }

    @Operation(summary = "Actualizar plan de nutrición")
    @PutMapping("/planes/{id}")
    public ResponseEntity<ApiResponseDTO<PlanNutricionResponseDTO>> actualizarPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody PlanNutricionRequestDTO request
    ) {
        PlanNutricionResponseDTO response = nutricionService.actualizarPlan(principal.getId(), id, request);

        return ResponseEntity.ok(ApiResponseDTO.success(response, "Plan de nutrición actualizado correctamente"));
    }

    @Operation(summary = "Activar un plan de nutrición")
    @PatchMapping("/planes/{planId}/activar")
    public ResponseEntity<ApiResponseDTO<Void>> activarPlan(
            @AuthenticationPrincipal UserPrincipal entrenador,
            @PathVariable UUID planId) {

        nutricionService.activarPlan(entrenador.getId(), planId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desactivar un plan de nutrición")
    @PatchMapping("/planes/{planId}/desactivar")
    public ResponseEntity<Void> desactivarPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserPrincipal usuario
    ) {
        nutricionService.desactivarPlan(usuario.getId(), planId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar plan de nutrición")
    @DeleteMapping("/planes/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> eliminarPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        nutricionService.eliminarPlan(principal.getId(), id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Plan de nutrición eliminado correctamente"));
    }
}

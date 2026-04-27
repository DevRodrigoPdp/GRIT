package grit.sistema.backend.controller.nutrition;

import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.nutrition.AlimentoRecienteDTO;
import grit.sistema.backend.dto.nutrition.AlimentoRecienteRequestDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionRequestDTO;
import grit.sistema.backend.dto.nutrition.PlanNutricionResponseDTO;
import grit.sistema.backend.security.user.UserPrincipal;
import grit.sistema.backend.service.nutrition.NutricionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nutricion")
@Tag(name = "Nutrición")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTRENADOR') and @auth.tieneTituloNutricion()")
@Slf4j
public class NutricionController {
    private final NutricionService nutricionService;

    @Operation(summary = "Listar planes de nutrición")
    @GetMapping("/planes")
    public ResponseEntity<ApiResponseDTO<List<PlanNutricionDTO>>> listarPlanes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID atletaId
    ) {
        log.info("Listando planes de nutrición para entrenador {} y atleta {}", principal.getEmail(), atletaId);
        List<PlanNutricionDTO> planes = nutricionService.listarPlanes(principal.getId(), atletaId);
        return ResponseEntity.ok(ApiResponseDTO.success(planes, "Planes de nutrición encontrados"));
    }

    @Operation(summary = "Crear plan de nutrición")
    @PostMapping("/planes")
    public ResponseEntity<ApiResponseDTO<PlanNutricionResponseDTO>> crearPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PlanNutricionRequestDTO request
    ) {
        log.info("Creando plan de nutrición para entrenador {} y atleta {}", principal.getEmail(), request.atletaId());
        PlanNutricionResponseDTO response = nutricionService.crearPlan(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(response, "Plan de nutrición creado correctamente"));
    }

    @Operation(summary = "Eliminar plan de nutrición")
    @DeleteMapping("/planes/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> eliminarPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        log.info("Eliminando plan de nutrición {} por entrenador {}", id, principal.getEmail());
        nutricionService.eliminarPlan(principal.getId(), id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Plan de nutrición eliminado correctamente"));
    }

    @Operation(summary = "Listar alimentos recientes por comida")
    @GetMapping("/recientes")
    public ResponseEntity<ApiResponseDTO<List<AlimentoRecienteDTO>>> listarRecientes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String comida
    ) {
        log.info("Listando alimentos recientes para entrenador {} y comida {}", principal.getEmail(), comida);
        List<AlimentoRecienteDTO> recientes = nutricionService.listarAlimentosRecientes(principal.getId(), comida);
        return ResponseEntity.ok(ApiResponseDTO.success(recientes, "Alimentos recientes encontrados"));
    }

    @Operation(summary = "Registrar alimento reciente")
    @PostMapping("/recientes")
    public ResponseEntity<ApiResponseDTO<Void>> registrarReciente(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AlimentoRecienteRequestDTO request
    ) {
        log.info("Registrando alimento reciente para entrenador {} y comida {}", principal.getEmail(), request.nombreComida());
        nutricionService.registrarAlimentoReciente(principal.getId(), request);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Alimento reciente registrado correctamente"));
    }
}

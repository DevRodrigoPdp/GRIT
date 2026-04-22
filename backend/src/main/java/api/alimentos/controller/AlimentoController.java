package api.alimentos.controller;

import api.alimentos.dto.AlimentoCrearRequestDTO;
import api.alimentos.dto.AlimentoResponseDTO;
import api.alimentos.service.AlimentoService;
import grit.sistema.backend.dto.ApiResponseDTO;
import grit.sistema.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * Controlador REST para gestionar la API de alimentos.
 * Endpoints:
 * - GET /api/v1/alimentos?q=<texto> - Buscar alimentos
 * - POST /api/v1/alimentos - Crear alimento (solo entrenadores con título en nutrición)
 */
@RestController
@RequestMapping("/api/v1/alimentos")
@Tag(name = "Alimentos", description = "API de gestión de alimentos para nutrición")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTRENADOR') and @auth.tieneTituloNutricion()")
@Slf4j
public class AlimentoController {

    private final AlimentoService alimentoService;

    /**
     * Busca alimentos por nombre o marca.
     * Devuelve máximo 15 resultados ordenados por relevancia.
     * Si q tiene menos de 2 caracteres, devuelve una lista vacía sin error.
     *
     * @param q Término de búsqueda (mínimo 2 caracteres)
     * @return Lista de alimentos encontrados
     */
    @Operation(summary = "Buscar alimentos por nombre o marca")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AlimentoResponseDTO>>> buscarAlimentos(
            @Parameter(description = "Término de búsqueda (mínimo 2 caracteres)", example = "pollo")
            @RequestParam(name = "q", required = false, defaultValue = "") String q
    ) {
        log.info("Búsqueda de alimentos: '{}'", q);
        List<AlimentoResponseDTO> alimentos = alimentoService.buscarAlimentos(q);
        return ResponseEntity.ok(
                ApiResponseDTO.success(alimentos, "Búsqueda completada")
        );
    }

    /**
     * Crea un nuevo alimento personalizado.
     * Solo disponible para entrenadores con titulación en nutrición.
     *
     * @param request Datos del alimento a crear
     * @param principal Usuario autenticado
     * @return El alimento creado con status 201
     */
    @Operation(summary = "Crear un nuevo alimento personalizado")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<AlimentoResponseDTO>> crearAlimento(
            @Valid @RequestBody AlimentoCrearRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Creando alimento: {} (usuario: {})", request.getNombre(), principal.getEmail());
        AlimentoResponseDTO alimento = alimentoService.crearAlimento(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(alimento, "Alimento creado correctamente"));
    }
}

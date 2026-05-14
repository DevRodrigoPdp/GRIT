package grit.sistema.backend.controller.nutrition;

import grit.sistema.backend.dto.common.ApiResponseDTO;
import grit.sistema.backend.dto.nutrition.AlimentoCrearRequestDTO;
import grit.sistema.backend.dto.nutrition.AlimentoResponseDTO;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.nutrition.AlimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
     * BUSCADOR GLOBAL (Omnibox)
     * Busca en nombre, marca y categoría simultáneamente con lógica de relevancia.
     * GET /api/v1/alimentos/search?q=avena
     */
    @Operation(summary = "Buscador global de alimentos por término único")
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Page<AlimentoResponseDTO>>> buscadorGlobal(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        String query = (q != null) ? q.trim() : "";
        Page<AlimentoResponseDTO> resultados = alimentoService.buscadorGlobal(query, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success(resultados, "Resultados globales de alimentos encontrados"));
    }

    /**
     * BÚSQUEDA POR FILTROS ESPECÍFICOS
     * GET /api/v1/alimentos?q=pollo&categoria=Carnes
     */
    @Operation(summary = "Filtrar alimentos por nombre/marca y categoría")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Page<AlimentoResponseDTO>>> listarConFiltros(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @PageableDefault(size = 15, sort = "nombre") Pageable pageable
    ) {
        Page<AlimentoResponseDTO> resultados = alimentoService.buscarAlimentos(q, categoria, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success(resultados, "Listado filtrado obtenido"));
    }

    @Operation(summary = "Obtener lista de todas las categorías de alimentos disponibles")
    @GetMapping("/categorias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<List<String>>> obtenerCategorias() {
        List<String> categorias = alimentoService.listarCategorias();
        return ResponseEntity.ok(ApiResponseDTO.success(categorias, "Categorías obtenidas"));
    }

    @Operation(summary = "Crear un nuevo alimento personalizado")
    @PostMapping
    @PreAuthorize("hasRole('ENTRENADOR') and @auth.tieneTituloNutricion()")
    public ResponseEntity<ApiResponseDTO<AlimentoResponseDTO>> crearAlimento(
            @Valid @RequestBody AlimentoCrearRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AlimentoResponseDTO alimento = alimentoService.crearAlimento(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(alimento, "Alimento creado correctamente"));
    }
}

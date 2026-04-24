package grit.sistema.backend.modules.alimentos.controller;

import grit.sistema.backend.dto.ApiResponseDTO;
import grit.sistema.backend.modules.alimentos.dto.AlimentoCrearRequestDTO;
import grit.sistema.backend.modules.alimentos.dto.AlimentoResponseDTO;
import grit.sistema.backend.modules.alimentos.service.AlimentoService;
import grit.sistema.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    @Operation(summary = "Buscar alimentos por nombre, marca o categoría")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Page<AlimentoResponseDTO>>> buscarAlimentos(
            @Parameter(description = "Búsqueda por nombre o marca")
            @RequestParam(name = "q", required = false) String q,

            @Parameter(description = "Filtrar por categoría específica")
            @RequestParam(name = "categoria", required = false) String categoria,

            @PageableDefault(size = 15, sort = "nombre") Pageable pageable
    ) {
        log.debug("Búsqueda Alimentos - q: '{}', cat: '{}', page: {}", q, categoria, pageable);

        // Pasamos ambos parámetros al service
        Page<AlimentoResponseDTO> alimentos = alimentoService.buscarAlimentos(q, categoria, pageable);

        return ResponseEntity.ok(ApiResponseDTO.success(alimentos, "Resultados encontrados"));
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
        log.info("Creando alimento: {} (usuario: {})", request.getNombre(), principal.getEmail());
        AlimentoResponseDTO alimento = alimentoService.crearAlimento(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(alimento, "Alimento creado correctamente"));
    }
}

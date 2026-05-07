package grit.sistema.backend.controller.training;

import grit.sistema.backend.dto.training.EjercicioDTO;
import grit.sistema.backend.service.training.EjercicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ejercicios")
@RequiredArgsConstructor
@Tag(name = "Ejercicios", description = "API para la consulta de la biblioteca de ejercicios")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class EjercicioController {
    private final EjercicioService ejercicioService;

    /**
     * BUSCADOR GLOBAL (Omnibox)
     * Busca en nombre y grupo muscular simultáneamente.
     * GET /api/v1/ejercicios/search?q=mancuerna
     */
    @Operation(summary = "Buscador global de ejercicios por término único")
    @GetMapping("/search")
    public ResponseEntity<Page<EjercicioDTO>> buscadorGlobal(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        String query = (q != null) ? q.trim() : "";
        Page<EjercicioDTO> resultados = ejercicioService.buscadorGlobal(query, pageable);
        return ResponseEntity.ok(resultados);
    }

    /**
     * Endpoint para buscar ejercicios.
     * Ejemplo: GET /api/v1/ejercicios?nombre=pull&grupoMuscular=Espalda&page=0&size=5
     */
    @Operation(summary = "Listar y filtrar ejercicios de forma paginada")
    @GetMapping
    public ResponseEntity<Page<EjercicioDTO>> listarEjercicios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String grupoMuscular,
            @PageableDefault(size = 15, sort = "nombre") Pageable pageable
    ) {
        Page<EjercicioDTO> respuesta = ejercicioService.buscarEjercicios(nombre, grupoMuscular, pageable);
        return ResponseEntity.ok(respuesta);
    }


}

package grit.sistema.backend.controller;

import grit.sistema.backend.dto.EntrenamientoRequestDTO;
import grit.sistema.backend.dto.EntrenamientoResponseDTO;
import grit.sistema.backend.service.EntrenamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Entrenamientos")
@RestController
@RequestMapping("/api/v1/entrenamientos")
@RequiredArgsConstructor
@Slf4j
public class EntrenamientoController {
    private final EntrenamientoService entrenamientoService;

    @Operation(summary = "Crear un entrenamiento",
            description = "Crea un entrenamiento con sus ejercicios asignados.")
    @PostMapping
    public ResponseEntity<EntrenamientoResponseDTO> crearEntrenamiento(
            @RequestBody EntrenamientoRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(entrenamientoService.guardarEntrenamiento(request, authentication.getName()));
    }

    @Operation(summary = "Listar mis entrenamientos",
            description = "Devuelve una lista de entrenamientos pertenecientere al usuario autenticado.")
    @GetMapping
    public ResponseEntity<List<EntrenamientoResponseDTO>> listarEntrenamientos(Authentication authentication) {
        log.info("Listando todos los entrenamientos");

        return ResponseEntity.ok(entrenamientoService.listarEntrenamientosPorUsuario(authentication.getName()));
    }

    @Operation(summary = "Eliminar un entrenamiento",
            description = "Borra el entrenamiento y todos sus ejercicios asociados mediante el UUID.")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> eliminarEntrenamiento(@PathVariable String uuid, Authentication authentication) {
        log.info("Eliminando un entrenamiento {}", uuid);

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        entrenamientoService.eliminarEntrenamientoPorUuid(uuid, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

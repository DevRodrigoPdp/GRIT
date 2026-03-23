package com.sistema.gritfitprueba.controller;

import com.sistema.gritfitprueba.dto.EntrenamientoRequestDTO;
import com.sistema.gritfitprueba.dto.EntrenamientoResponseDTO;
import com.sistema.gritfitprueba.model.Usuario;
import com.sistema.gritfitprueba.service.EntrenamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Entrenamientos")
@RestController
@RequestMapping("/api/entrenamientos")
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
    public ResponseEntity<EntrenamientoResponseDTO> eliminarEntrenamiento(@PathVariable String uuid, @AuthenticationPrincipal Usuario usuarioAutenticado) {
        log.info("Eliminando un entrenamiento {}", uuid);
        entrenamientoService.eliminarEntrenamientoPorUuid(uuid, usuarioAutenticado.getEmail());
        return ResponseEntity.noContent().build();
    }
}

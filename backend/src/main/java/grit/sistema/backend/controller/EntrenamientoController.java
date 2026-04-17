package grit.sistema.backend.controller;

import grit.sistema.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entrenamiento")
@Tag(name = "Entrenamiento")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENTRENADOR') and @auth.tieneTituloEntrenamiento()")
@Slf4j
public class EntrenamientoController {

    // Ejemplo de endpoint profesional
    @PostMapping("/planes")
    public ResponseEntity<Void> crearPlanNutricional() {
        log.info("Entrenador {} creando plan nutricional");
        // El servicio se encarga de la magia
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

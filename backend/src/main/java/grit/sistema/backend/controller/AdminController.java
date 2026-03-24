package grit.sistema.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<?> obtenerEstadísticas(){
        return ResponseEntity.ok(Map.of(
                "mensaje", "Bienvenido al panel de Control de Gritfit",
                "usuario",1250,
                "suscripciones_premium", 450,
                "estado_servidor", "Óptimo"));

    }
}

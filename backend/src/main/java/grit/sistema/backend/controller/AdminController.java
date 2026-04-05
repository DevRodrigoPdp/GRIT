package grit.sistema.backend.controller;

import grit.sistema.backend.dto.UsuarioDTO;
import grit.sistema.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Administración")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        log.info("Iniciando getAllUsuarios");
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("usuarios/{uuid}")
    public ResponseEntity<UsuarioDTO> getUsuario(@PathVariable UUID uuid) {
        log.info("Iniciando getUsuario: {}", uuid);
        return ResponseEntity.ok(usuarioService.findByUuid(uuid));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDTO> createUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        log.info("Iniciando createUsuario: {}", usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(usuarioDTO));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> obtenerEstadisticas(){
        return ResponseEntity.ok(Map.of(
                "mensaje", "Bienvenido al panel de Control de Gritfit",
                "usuario",1250,
                "suscripciones_premium", 450,
                "estado_servidor", "Óptimo"));

    }
}

package grit.sistema.backend.controller.admin;

import grit.sistema.backend.dto.coaching.EntrenadorPendienteDTO;
import grit.sistema.backend.dto.usuario.UsuarioDTO;
import grit.sistema.backend.service.admin.AdminService;
import grit.sistema.backend.service.auth.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Administración")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        log.info("Iniciando getAllUsuarios");
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @Operation(summary = "Listar entrenadores pendientes de revisión")
    @GetMapping("/entrenadores/pendientes")
    public ResponseEntity<Page<EntrenadorPendienteDTO>> getPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.obtenerPendientes(page, size));
    }

    @Operation(summary = "Aprobar o rechazar un entrenador")
    @PostMapping("/entrenadores/{id}/revision")
    public ResponseEntity<Void> procesarRevision(
            @PathVariable UUID id,
            @RequestParam boolean aprobado,
            @RequestParam(required = false) String motivo) {
        adminService.procesarAprobacion(id, aprobado, motivo);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar definitivamente un usuario y sus archivos")
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable UUID id) {
        adminService.eliminarEntrenadorDefinitivo(id);
        return ResponseEntity.noContent().build();
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

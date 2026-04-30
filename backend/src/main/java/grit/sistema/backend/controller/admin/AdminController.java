package grit.sistema.backend.controller.admin;

import grit.sistema.backend.dto.coaching.EntrenadorPendienteDTO;
import grit.sistema.backend.dto.usuario.UsuarioDTO;
import grit.sistema.backend.service.admin.AdminService;
import grit.sistema.backend.service.usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/usuarios/search")
    public ResponseEntity<Page<UsuarioDTO>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return ResponseEntity.ok(adminService.buscarUsuarios(search, pageable));
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
        adminService.eliminarUsuarioCompleto(id);
        return ResponseEntity.noContent().build();
    }
}

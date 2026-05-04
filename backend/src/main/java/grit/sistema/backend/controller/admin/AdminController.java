package grit.sistema.backend.controller.admin;

import grit.sistema.backend.dto.coaching.EntrenadorBusquedaDTO;
import grit.sistema.backend.dto.coaching.EntrenadorPendienteDTO;
import grit.sistema.backend.dto.user.UsuarioBusquedaDTO;
import grit.sistema.backend.dto.user.UsuarioResponseDTO;
import grit.sistema.backend.service.admin.AdminService;
import grit.sistema.backend.service.user.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        log.info("Iniciando getAllUsuarios");
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @Operation(summary = "Listar usuarios con búsqueda flexible por nombre o correo")
    @GetMapping("/usuarios/search")
    public ResponseEntity<Page<UsuarioBusquedaDTO>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        String query = (search != null) ? search.trim() : "";
        log.info("Admin buscando usuarios: '{}' [Página: {}]", query, page);
        return ResponseEntity.ok(adminService.buscarUsuarios(query, pageable));
    }

    @Operation(summary = "Listar entrenadores pendientes de revisión")
    @GetMapping("/entrenadores/pendientes")
    public ResponseEntity<Page<EntrenadorPendienteDTO>> getPendientes(
            @RequestParam(defaultValue = "0") @Min(0) @Max(100) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(adminService.obtenerPendientes(page, size));
    }

    @Operation(summary = "Listar entrenadores pendientes con búsqueda flexible por nombre o correo")
    @GetMapping("/entrenadores/pendientes/search")
    public ResponseEntity<Page<EntrenadorBusquedaDTO>> getPendientes(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int size) {
        log.info("Admin solicitando lista de entrenadores pendientes. Filtro: '{}'", search);

        Page<EntrenadorBusquedaDTO> resultado = adminService.obtenerPendientesBuscador(search, page, size);

        return ResponseEntity.ok(resultado);
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

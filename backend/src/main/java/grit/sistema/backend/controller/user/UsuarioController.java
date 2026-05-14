package grit.sistema.backend.controller.user;

import grit.sistema.backend.dto.user.UsuarioDTO;
import grit.sistema.backend.service.user.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Slf4j
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> obtenerPerfil() {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioActual());
    }
}

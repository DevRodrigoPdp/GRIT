package grit.sistema.backend.controller;

import grit.sistema.backend.dto.usuario.UsuarioDTO;
import grit.sistema.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> obtenerPerfil() {
        log.info("Iniciando obtenerPerfil");
        return ResponseEntity.ok(usuarioService.obtenerUsuarioActual());
    }
}

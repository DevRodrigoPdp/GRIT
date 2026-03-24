package grit.sistema.backend.controller;

import grit.sistema.backend.dto.UsuarioDTO;
import grit.sistema.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        log.info("Iniciando getAllUsuarios");
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UsuarioDTO> getUsuario(@PathVariable UUID uuid) {
        log.info("Iniciando getUsuario: {}", uuid);
        return ResponseEntity.ok(usuarioService.findByUuid(uuid));
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> obtenerPerfil() {
        log.info("Iniciando obtenerPerfil");
        return ResponseEntity.ok(usuarioService.obtenerUsuarioActual());
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> createUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        log.info("Iniciando createUsuario: {}", usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(usuarioDTO));
    }
}

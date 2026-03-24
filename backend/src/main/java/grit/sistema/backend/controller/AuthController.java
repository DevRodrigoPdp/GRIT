package grit.sistema.backend.controller;

import grit.sistema.backend.dto.AuthResponseDTO;
import grit.sistema.backend.dto.LoginRequestDTO;
import grit.sistema.backend.dto.RegistroRequestDTO;
import grit.sistema.backend.dto.UsuarioDTO;
import grit.sistema.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<String> registrar(@RequestBody RegistroRequestDTO registroDTO) {
        usuarioService.registrar(registroDTO);
        return ResponseEntity.ok("Usuario Registrado con éxito");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDto) {
        // Log de nivel INFO: Rastreamos la intención
        log.info(">>> Solicitud de login recibida para el email: {}", loginDto.email());

        AuthResponseDTO response = usuarioService.login(loginDto);

        // Log de éxito
        log.info("<<< Login exitoso para el usuario con UUID: {}", response.usuario().idPublico());
        return ResponseEntity.ok(response);
    }
}

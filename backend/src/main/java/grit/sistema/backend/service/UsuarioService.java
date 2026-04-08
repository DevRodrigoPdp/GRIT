package grit.sistema.backend.service;

import grit.sistema.backend.dto.*;
import grit.sistema.backend.exception.SesionActivaException;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.UsuarioMapper;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.pepper}")
    private String pepper;

    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toDTO).toList();
    }

    public LoginData obtenerDatosParaRefresh(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuarioMapper.toLoginData(usuario);
    }

    public UsuarioDTO findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado con el email: " + email));

        return usuarioMapper.toDTO(usuario);
    }

    public UsuarioDTO guardar(UsuarioDTO usuarioDTO) {
        log.info("Iniciando creación de usuario para: {}", usuarioDTO.email());

        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        String passwordWithPepper = usuarioDTO.password() + pepper;
        usuario.setPassword(passwordEncoder.encode(passwordWithPepper));

        Usuario guardado = usuarioRepository.save(usuario);

        return usuarioMapper.toDTO(guardado);
    }

    public UsuarioDTO findByUuid(UUID uuid) {
        Usuario usuario = usuarioRepository.findById(uuid).orElseThrow(() -> new RuntimeException("Usuario no encontrado con el UUID: " + uuid));

        return usuarioMapper.toDTO(usuario);
    }

    public LoginResponseDTO login(LoginRequestDTO loginDto) {
        log.info(">>> Intentando autenticar usuario: {}", loginDto.email());

        String passwordWithPepper = loginDto.password() + pepper;

        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.email(),
                        passwordWithPepper)
        );

        Usuario usuario = (Usuario) auth.getPrincipal();

        LoginData data = usuarioMapper.toLoginData(usuario);

        log.info("<<< Autenticación exitosa para: {}", usuario.getEmail());

        return new LoginResponseDTO(true, data);
    }

    public UsuarioDTO obtenerUsuarioActual() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SesionActivaException("No hay sesión activa");
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuarioMapper.toDTO(usuario);
    }
}

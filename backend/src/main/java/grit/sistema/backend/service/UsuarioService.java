package grit.sistema.backend.service;

import grit.sistema.backend.dto.AuthResponseDTO;
import grit.sistema.backend.dto.LoginRequestDTO;
import grit.sistema.backend.dto.RegistroRequestDTO;
import grit.sistema.backend.dto.UsuarioDTO;
import grit.sistema.backend.exception.SesionActivaException;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.UsuarioMapper;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toDTO).toList();
    }

    public UsuarioDTO guardar(UsuarioDTO usuarioDTO) {
        log.info("Iniciando creación de usuario para: {}", usuarioDTO.email());

        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        usuario.setPassword(passwordEncoder.encode(usuarioDTO.password()));

        Usuario guardado = usuarioRepository.save(usuario);

        return usuarioMapper.toDTO(guardado);
    }

    public UsuarioDTO findByUuid(UUID uuid) {
        Usuario usuario = usuarioRepository.findByUuid(uuid).orElseThrow(() -> new RuntimeException("Usuario no encontrado con el UUID: " + uuid));

        return usuarioMapper.toDTO(usuario);
    }

    public AuthResponseDTO login(LoginRequestDTO loginDto) {
        log.info("Iniciando cuenta para login: {}", loginDto.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
        );

        log.info("Usuario encontrado, verificando password...");

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.email());

        Usuario usuario = (Usuario) userDetails;

        String miTokenGenerado = jwtService.generarToken(usuario.getEmail(),usuario.getRol().name());

        UsuarioDTO usuarioDTO = usuarioMapper.toDTO(usuario);

        log.info("Token generado con éxito: {}", miTokenGenerado);
        return new AuthResponseDTO(miTokenGenerado, usuarioDTO);
    }

    public void registrar(RegistroRequestDTO registroDto) {
        if (usuarioRepository.existsByEmail(registroDto.email())) {
            throw new UsuarioExistenteException("Usuario existente");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(registroDto.username());
        usuario.setEmail(registroDto.email());
        usuario.setPassword(passwordEncoder.encode(registroDto.password()));
        usuario.setRol(Rol.USER);
        usuarioRepository.save(usuario);
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

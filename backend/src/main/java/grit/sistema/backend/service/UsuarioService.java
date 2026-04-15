package grit.sistema.backend.service;

import grit.sistema.backend.dto.auth.MeResponseDTO;
import grit.sistema.backend.dto.login.LoginData;
import grit.sistema.backend.dto.login.LoginRequestDTO;
import grit.sistema.backend.dto.login.LoginResponseDTO;
import grit.sistema.backend.dto.usuario.UsuarioDTO;
import grit.sistema.backend.exception.SesionActivaException;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.UsuarioMapper;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public UsuarioDTO guardar(UsuarioDTO usuarioDTO) {
        log.info("Iniciando creación de usuario para: {}", usuarioDTO.email());

        if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new UsuarioExistenteException("El correo electrónico ya está registrado");
        }

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

    @Transactional(readOnly = true)
    public MeResponseDTO obtenerMiInformacion(String email) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SesionActivaException("No hay sesión activa");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        MeResponseDTO.MeData meData;

        if (usuario instanceof Entrenador e) {
            meData = new MeResponseDTO.MeData(
                    e.getId(),
                    e.getNombre(),
                    e.getRol().name(),
                    e.getEstado().name(),
                    null, // servicio es null para entrenadores
                    e.getTitulacionEntrenamiento() != null,
                    e.getTitulacionNutricion() != null,
                    e.getTitulacionEntrenamiento() != null ? e.getTitulacionEntrenamiento().name() : null,
                    e.getTitulacionNutricion() != null ? e.getTitulacionNutricion().name() : null
            );
        } else if (usuario instanceof Atleta a) {
            meData = new MeResponseDTO.MeData(
                    a.getId(),
                    a.getNombre(),
                    a.getRol().name(),
                    a.getEstado().name(),
                    a.getServicio() != null ? a.getServicio().name() : null,
                    null, // tituloEntrenamiento es null para atletas
                    null, // tituloNutricion es null para atletas
                    null,
                    null
            );
        } else {
            // Caso genérico (Admin)
            meData = new MeResponseDTO.MeData(
                    usuario.getId(), usuario.getNombre(), usuario.getRol().name(),
                    usuario.getEstado().name(), null, null, null, null, null
            );
        }

        return new MeResponseDTO(true, meData);
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

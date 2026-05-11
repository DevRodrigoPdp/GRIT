package grit.sistema.backend.service.user.impl;

import grit.sistema.backend.clientAPI.PwnedPasswordClient;
import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.user.MeResponseDTO;
import grit.sistema.backend.dto.user.UsuarioDTO;
import grit.sistema.backend.dto.user.UsuarioResponseDTO;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.exception.business.SesionActivaException;
import grit.sistema.backend.exception.security.PwnedPasswordException;
import grit.sistema.backend.mapper.user.UsuarioMapper;
import grit.sistema.backend.repository.user.UsuarioRepository;
import grit.sistema.backend.service.user.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PwnedPasswordClient pwnedClient;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginData obtenerDatosParaRefresh(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuarioMapper.toLoginData(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuarioMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public MeResponseDTO obtenerMiInformacion(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        MeResponseDTO.MeData meData;

        if (usuario instanceof Entrenador e) {
            meData = new MeResponseDTO.MeData(
                    e.getId(),
                    e.getNombre(),
                    e.getRol().name(),
                    e.getEstado().name(),
                    e.getEstadoRevision().name(),
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
                    null,
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
                    usuario.getEstado().name(),null, null, null, null, null, null
            );
        }

        return new MeResponseDTO(true, meData);
    }

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional
    public void actualizarPassword(UUID usuarioId, PasswordUpdateDTO dto) {
        if (pwnedClient.isPasswordPwned(dto.nueva())) {
            throw new PwnedPasswordException("Seguridad insuficiente: Contraseña detectada en filtraciones de datos.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // 3. Validar password actual
        if (!passwordEncoder.matches(dto.actual(), usuario.getPassword())) {
            throw new BadCredentialsException("PASSWORD_INCORRECTO");
        }

        usuario.setPassword(passwordEncoder.encode(dto.nueva()));
    }

    @Override
    @Transactional
    public void suspenderUsuario(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        usuario.setEstado(EstadoUsuario.SUSPENDIDO);

        usuarioRepository.save(usuario);
    }
}

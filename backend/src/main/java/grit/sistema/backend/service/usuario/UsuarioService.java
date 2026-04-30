package grit.sistema.backend.service.usuario;

import grit.sistema.backend.dto.usuario.MeResponseDTO;
import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.auth.LoginRequestDTO;
import grit.sistema.backend.dto.auth.LoginResponseDTO;
import grit.sistema.backend.dto.usuario.UsuarioDTO;
import grit.sistema.backend.exception.business.SesionActivaException;
import grit.sistema.backend.mapper.usuario.UsuarioMapper;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.repository.usuario.UsuarioRepository;
import grit.sistema.backend.security.user.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public LoginData obtenerDatosParaRefresh(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuarioMapper.toLoginData(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuarioMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
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

    @Transactional
    public void suspenderUsuario(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        usuario.setEstado(EstadoUsuario.SUSPENDIDO);
        // No es estrictamente necesario llamar a save() si estamos en una transacción,
        // pero ayuda a la legibilidad para un desarrollador junior.
        usuarioRepository.save(usuario);
    }


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
}

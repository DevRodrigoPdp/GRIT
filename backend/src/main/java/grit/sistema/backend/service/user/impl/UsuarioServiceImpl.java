package grit.sistema.backend.service.user.impl;

import grit.sistema.backend.clientAPI.PwnedPasswordClient;
import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.coaching.FotoPerfilResponseDTO;
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
import grit.sistema.backend.service.common.StorageService;
import grit.sistema.backend.service.user.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final StorageService storageService;

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
                    null,
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
                    null,
                    null,
                    null,
                    null
            );
        } else {
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
    public FotoPerfilResponseDTO actualizarFotoPerfil(UUID usuarioId, MultipartFile foto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        String carpeta;
        String urlAntigua;

        if (usuario instanceof Entrenador e) {
            carpeta = "profiles/entrenadores";
            urlAntigua = e.getFotoUrl();
        } else if (usuario instanceof Atleta a) {
            carpeta = "profiles/atletas";
            urlAntigua = a.getFotoUrl();
        } else {
            throw new IllegalArgumentException("Tipo de usuario no soportado");
        }

        String fotoKey = storageService.uploadProfilePhoto(foto, carpeta);

        if (urlAntigua != null && !urlAntigua.isBlank()) {
            storageService.deleteFile(urlAntigua);
        }

        if (usuario instanceof Entrenador e) e.setFotoUrl(fotoKey);
        if (usuario instanceof Atleta a) a.setFotoUrl(fotoKey);

        return new FotoPerfilResponseDTO(storageService.getPresignedUrl(fotoKey));
    }

    @Override
    @Transactional
    public void actualizarPassword(UUID usuarioId, PasswordUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.actual(), usuario.getPassword())) {
            throw new BadCredentialsException("PASSWORD_INCORRECTO");
        }

        try {
            if (pwnedClient.isPasswordPwned(dto.nueva())) {
                throw new PwnedPasswordException("Seguridad insuficiente: Contraseña detectada en filtraciones.");
            }
        } catch (Exception e) {
            log.warn("No se pudo verificar Pwned API, procediendo con cambio estándar.");
        }

        usuario.setPassword(passwordEncoder.encode(dto.nueva()));
    }

    @Override
    @Transactional
    public void suspenderUsuario(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        usuario.setEstado(EstadoUsuario.SUSPENDIDO);
    }
}

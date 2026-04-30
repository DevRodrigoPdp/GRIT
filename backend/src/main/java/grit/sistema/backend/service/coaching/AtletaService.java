package grit.sistema.backend.service.coaching;

import grit.sistema.backend.clientAPI.PwnedPasswordClient;
import grit.sistema.backend.dto.coaching.ProfesionalAsignadoDTO;
import grit.sistema.backend.dto.coaching.AtletaPerfilDTO;
import grit.sistema.backend.dto.coaching.AtletaRequestDTO;
import grit.sistema.backend.dto.coaching.AtletaResponseDTO;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.exception.security.PwnedPasswordException;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.mapper.coaching.AtletaMapper;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.service.common.StorageService;
import grit.sistema.backend.service.user.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AtletaService {
    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsignacionRepository asignacionRepository;
    private final UsuarioService usuarioService;
    private final PwnedPasswordClient pwnedClient;
    private final StorageService storageService;
    private final AtletaMapper atletaMapper;
    private final AtletaPersistenceService persistenceService;

    public AtletaResponseDTO registrarAtleta(AtletaRequestDTO dto, MultipartFile foto) {
        if (pwnedClient.isPasswordPwned(dto.password())) {
            throw new PwnedPasswordException("Seguridad insuficiente: Contraseña detectada en filtraciones de datos.");
        }

        String fotoKey = storageService.uploadAtletaFoto(foto);

        return persistenceService.guardarAtleta(dto, fotoKey);
    }

    @Transactional(readOnly = true)
    public AtletaPerfilDTO obtenerPerfil(String email) {
        return atletaRepository.findByEmail(email)
                .map(atletaMapper::toPerfilDTO)
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<ProfesionalAsignadoDTO> getProfesionalesAsignados(UUID atletaId) {
        List<Asignacion> asignaciones = asignacionRepository.findAsignacionesActivas(atletaId);
        List<ProfesionalAsignadoDTO> resultado = new ArrayList<>();

        for (Asignacion asig : asignaciones) {
            Entrenador e = asig.getEntrenador();
            TipoServicio servicio = asig.getTipoServicio();

            // Lógica para Entrenamiento
            if (servicio == TipoServicio.ENTRENAMIENTO || servicio == TipoServicio.AMBOS) {
                String titulo = (e.getTitulacionEntrenamiento() != null)
                        ? e.getTitulacionEntrenamiento().name()
                        : "Certificación Profesional";

                resultado.add(new ProfesionalAsignadoDTO(e.getId(), e.getNombre(), titulo, "ENTRENADOR", e.getDescripcion()));
            }

            // Lógica para Nutrición
            if (servicio == TipoServicio.NUTRICION || servicio == TipoServicio.AMBOS) {
                String titulo = (e.getTitulacionNutricion() != null)
                        ? e.getTitulacionNutricion().name()
                        : "Dietista Registrado";

                resultado.add(new ProfesionalAsignadoDTO(e.getId(), e.getNombre(), titulo, "NUTRICIONISTA", e.getDescripcion()));
            }
        }
        return resultado;
    }

    @Transactional
    public void cambiarPassword(String email, PasswordUpdateDTO dto) {
        if (pwnedClient.isPasswordPwned(dto.nueva())) {
            throw new PwnedPasswordException("Seguridad insuficiente: Contraseña detectada en filtraciones de datos.");
        }

        Atleta atleta = atletaRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));

        if (!passwordEncoder.matches(dto.actual(), atleta.getPassword())) {
            throw new BadCredentialsException("PASSWORD_INCORRECTO");
        }

        atleta.setPassword(passwordEncoder.encode(dto.nueva()));
        atletaRepository.save(atleta);
    }

    @Transactional
    public void solicitarBajaCuenta(UUID atletaId) {
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new EntityNotFoundException("Atleta no encontrado"));

        asignacionRepository.desactivarAsignacionesPorAtleta(atletaId);
        usuarioService.suspenderUsuario(atletaId);
    }

}

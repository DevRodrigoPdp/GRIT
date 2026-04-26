package grit.sistema.backend.service;

import grit.sistema.backend.client.PwnedPasswordClient;
import grit.sistema.backend.dto.atleta.ProfesionalAsignadoDTO;
import grit.sistema.backend.dto.atleta.AtletaPerfilDTO;
import grit.sistema.backend.dto.atleta.AtletaRequestDTO;
import grit.sistema.backend.dto.atleta.AtletaResponseDTO;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.training.RutinaDTO;
import grit.sistema.backend.exception.PwnedPasswordException;
import grit.sistema.backend.mapper.EntrenamientoMapper;
import grit.sistema.backend.model.coaching.Asignacion;
import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.model.enums.TipoServicio;
import grit.sistema.backend.model.training.Rutina;
import grit.sistema.backend.repository.AsignacionRepository;
import grit.sistema.backend.repository.AtletaRepository;
import grit.sistema.backend.repository.RutinaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AtletaService {
    private final AtletaRepository atletaRepository;
    private final EntrenamientoMapper entrenamientoMapper;
    private final RutinaRepository rutinaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsignacionRepository asignacionRepository;
    private final UsuarioService usuarioService;
    private final PwnedPasswordClient pwnedClient;
    private final StorageService storageService;
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
        Atleta a = atletaRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("Atleta no encontrado"));

        return new AtletaPerfilDTO(
                a.getId(), a.getNombre(), a.getEmail(),
                a.getFechaNac(), a.getGenero(), a.getPesoKg(),
                a.getAlturaCm(), a.getDeporte(), a.getNivel(),
                a.getServicio(), a.getObjetivo()
        );
    }

    @Transactional(readOnly = true)
    public Optional<RutinaDTO> getPlanActivoAtleta(UUID atletaId) {
        return rutinaRepository.findFirstByAtletaIdOrderByCreadoEnDesc(atletaId)
                .map(entrenamientoMapper::toDTO); // ¡Mucho más limpio!
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
        if (!atletaRepository.existsById(atletaId)) {
            throw new EntityNotFoundException("El perfil de atleta no existe");
        }

        asignacionRepository.desactivarAsignacionesPorAtleta(atletaId);

        usuarioService.suspenderUsuario(atletaId);
    }
}

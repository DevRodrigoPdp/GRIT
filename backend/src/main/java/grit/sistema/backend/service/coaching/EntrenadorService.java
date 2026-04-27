package grit.sistema.backend.service.coaching;

import grit.sistema.backend.clientAPI.PwnedPasswordClient;
import grit.sistema.backend.dto.coaching.AtletaResumenDTO;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.coaching.EntrenadorPerfilDTO;
import grit.sistema.backend.dto.coaching.EntrenadorRequestDTO;
import grit.sistema.backend.dto.coaching.EntrenadorResponseDTO;
import grit.sistema.backend.exception.PwnedPasswordException;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.EntrenadorMapper;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.TitulacionEntrenamiento;
import grit.sistema.backend.entity.coaching.enums.TitulacionNutricion;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.usuario.UsuarioRepository;
import grit.sistema.backend.service.common.StorageService;
import grit.sistema.backend.service.auth.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrenadorService {
    private final EntrenadorPersistenceService persistenceService;
    private final EntrenadorRepository entrenadorRepository;
    private final AsignacionRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;
    private final EntrenadorMapper entrenadorMapper;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final PwnedPasswordClient pwnedClient;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public EntrenadorResponseDTO registrarEntrenador(EntrenadorRequestDTO request, MultipartFile fotoPerfil, List<MultipartFile> certificaciones) {
        if (pwnedClient.isPasswordPwned(request.getPassword())) {
            throw new PwnedPasswordException("Seguridad insuficiente: Contraseña detectada en filtraciones de datos.");
        }

        validarRequisitosProfesionales(request);
        validarTamanoArchivos(certificaciones);

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }

        // 1. IO Externa (MinIO) - Fuera de transacción
        List<String> urls = Optional.ofNullable(certificaciones)
                .orElse(List.of())
                .stream()
                .map(storageService::uploadFile)
                .toList();

        String fotoKey = storageService.uploadEntrenadorFoto(fotoPerfil);

        // 2. Persistencia - Dentro de transacción
        try {
            Entrenador entrenador = persistenceService.guardarEntrenador(request, urls, certificaciones, fotoKey);
            return entrenadorMapper.toResponse(entrenador);
        } catch (Exception e) {
            log.error("Error en persistencia. Iniciando compensación de archivos en MinIO...");
            // SI LA DB FALLA, BORRAMOS LO SUBIDO
            urls.forEach(storageService::deleteFile);
            storageService.deleteFile(fotoKey);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public EntrenadorPerfilDTO obtenerPerfil(String email) {
        Entrenador e = entrenadorRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        return new EntrenadorPerfilDTO(
                e.getId(), e.getNombre(), e.getEmail(),
                e.getTitulacionEntrenamiento() != null ? e.getTitulacionEntrenamiento().name() : null,
                e.getTitulacionNutricion() != null ? e.getTitulacionNutricion().name() : null,
                e.getExperienciaAnos(), e.getDescripcion(), e.getEstado().name()
        );
    }

    @Transactional(readOnly = true)
    public List<AtletaResumenDTO> listarMisAtletas(String email) {
        List<Asignacion> asignaciones = asignacionRepository.findAllByEntrenadorEmailAndActivaTrue(email);

        // Agrupamos por Atleta para manejar el caso de "AMBOS" servicios
        Map<Atleta, List<Asignacion>> asignacionesPorAtleta = asignaciones.stream()
                .collect(Collectors.groupingBy(Asignacion::getAtleta));

        return asignacionesPorAtleta.entrySet().stream()
                .map(entry -> {
                    Atleta a = entry.getKey();
                    List<Asignacion> asigs = entry.getValue();

                    // Determinamos el string del servicio (ENTRENAMIENTO, NUTRICION o AMBOS)
                    String servicioLabel = determinarServicioLabel(asigs);

                    // Calculamos si tiene planes reales
                    boolean tienePlan = calcularSiTienePlanActivo(a, email);

                    return new AtletaResumenDTO(
                            a.getId(),
                            a.getNombre(),
                            a.getDeporte(), // Asegúrate de que Atleta tenga este campo
                            a.getNivel().name(),
                            servicioLabel,
                            tienePlan
                    );
                })
                .toList();
    }

    @Transactional
    public void cambiarPassword(String email, PasswordUpdateDTO dto) {
        if (pwnedClient.isPasswordPwned(dto.nueva())) {
            throw new PwnedPasswordException("Seguridad insuficiente: Contraseña detectada en filtraciones de datos.");
        }

        Entrenador entrenador = entrenadorRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        if (!passwordEncoder.matches(dto.actual(), entrenador.getPassword())) {
            throw new BadCredentialsException("PASSWORD_INCORRECTO");
        }

        entrenador.setPassword(passwordEncoder.encode(dto.nueva()));
        entrenadorRepository.save(entrenador);
    }

    @Transactional
    public void solicitarBajaCuenta(UUID entrenadorId) {
        if (!entrenadorRepository.existsById(entrenadorId)) {
            throw new EntityNotFoundException("El perfil de entrenador no existe");
        }

        asignacionRepository.desactivarAsignacionesPorEntrenador(entrenadorId);

        usuarioService.suspenderUsuario(entrenadorId);
    }

    private String determinarServicioLabel(List<Asignacion> asigs) {
        if (asigs.size() > 1) return "AMBOS";
        return asigs.get(0).getTipoServicio().name();
    }

    private boolean calcularSiTienePlanActivo(Atleta a, String entrenadorEmail) {
        // Aquí debes consultar tu PlanRepository
        // countByAtletaIdAndEntrenadorEmailAndActivoTrue > 0
        return asignacionRepository.existsByAtletaIdAndEntrenadorEmailAndActivaTrue(a.getId(), entrenadorEmail);
    }

    private void validarTamanoArchivos(List<MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty()) return;

        long totalSize = archivos.stream()
                .mapToLong(MultipartFile::getSize)
                .sum();

        if (totalSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El tamaño total de los archivos excede el límite de 10MB");
        }
    }

    private void validarRequisitosProfesionales(EntrenadorRequestDTO request) {
        if (request.getTitulacionEntrenamiento() == null && request.getTitulacionNutricion() == null) {
            throw new IllegalArgumentException("Debe tener al menos una titulación.");
        }

        boolean requiereColegiado = (request.getTitulacionEntrenamiento() == TitulacionEntrenamiento.GRADO_CAFYD) ||
                (request.getTitulacionNutricion() == TitulacionNutricion.GRADO_NUTRICION_DIETETICA);

        if (requiereColegiado && (request.getCodigoProfesional() == null || request.getCodigoProfesional().isBlank())) {
            throw new IllegalArgumentException("El código profesional es obligatorio para titulaciones de grado");
        }
    }
}
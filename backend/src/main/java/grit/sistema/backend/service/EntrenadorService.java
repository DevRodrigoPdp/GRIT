package grit.sistema.backend.service;

import grit.sistema.backend.dto.atleta.AtletaResumenDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorPerfilDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorRequestDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorResponseDTO;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.EntrenadorMapper;
import grit.sistema.backend.model.coaching.Asignacion;
import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import grit.sistema.backend.repository.AsignacionRepository;
import grit.sistema.backend.repository.EntrenadorRepository;
import grit.sistema.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public EntrenadorResponseDTO registrarEntrenador(EntrenadorRequestDTO request) {
        validarRequisitosProfesionales(request);
        validarTamanoArchivos(request.getDocumentos());

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }

        // 1. IO Externa (MinIO) - Fuera de transacción
        List<String> urls = Optional.ofNullable(request.getDocumentos())
                .orElse(List.of())
                .stream()
                .map(storageService::uploadFile)
                .toList();

        // 2. Persistencia - Dentro de transacción
        try {
            Entrenador entrenador = persistenceService.guardarEntrenador(request, urls);
            return entrenadorMapper.toResponse(entrenador);
        } catch (Exception e) {
            log.error("Error en persistencia. Iniciando compensación de archivos en MinIO...");
            // SI LA DB FALLA, BORRAMOS LO SUBIDO
            urls.forEach(storageService::deleteFile);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public EntrenadorPerfilDTO obtenerPerfil(String email){
        Entrenador e = entrenadorRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("Entrenador no encontrado"));

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
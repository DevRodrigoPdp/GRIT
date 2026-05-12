package grit.sistema.backend.service.coaching.impl;

import grit.sistema.backend.clientAPI.PwnedPasswordClient;
import grit.sistema.backend.dto.coaching.*;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Asignacion;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.DocumentoEntrenador;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.TitulacionEntrenamiento;
import grit.sistema.backend.entity.coaching.enums.TitulacionNutricion;
import grit.sistema.backend.exception.business.UsuarioExistenteException;
import grit.sistema.backend.exception.security.PwnedPasswordException;
import grit.sistema.backend.mapper.coaching.EntrenadorMapper;
import grit.sistema.backend.repository.coaching.AsignacionRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.user.UsuarioRepository;
import grit.sistema.backend.service.coaching.EntrenadorPersistenceService;
import grit.sistema.backend.service.coaching.EntrenadorService;
import grit.sistema.backend.service.common.StorageService;
import grit.sistema.backend.service.user.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class EntrenadorServiceImpl implements EntrenadorService {
    private final EntrenadorPersistenceService persistenceService;
    private final EntrenadorRepository entrenadorRepository;
    private final AsignacionRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;
    private final EntrenadorMapper entrenadorMapper;
    private final UsuarioService usuarioService;
    private final PwnedPasswordClient pwnedClient;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public EntrenadorResponseDTO registrarEntrenador(EntrenadorRequestDTO request, MultipartFile fotoPerfil, List<MultipartFile> certificaciones) {
        validarRequisitosProfesionales(request);
        validarTamanoArchivos(certificaciones);

        if (pwnedClient.isPasswordPwned(request.getPassword())) {
            throw new PwnedPasswordException("Seguridad insuficiente: Contraseña detectada en filtraciones de datos.");
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioExistenteException("EMAIL_DUPLICADO");
        }

        List<String> urls = subirCertificaciones(certificaciones);
        String fotoKey = subirFotoPerfil(fotoPerfil);

        try {
            Entrenador entrenador = persistenceService.guardarEntrenador(request, urls, certificaciones, fotoKey);
            return entrenadorMapper.toResponse(entrenador);
        } catch (Exception e) {
            log.error("Error en persistencia. Iniciando compensación de archivos en MinIO...");
            compensarArchivos(urls, fotoKey);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EntrenadorPerfilDTO obtenerPerfil(UUID entrenadorId) {
        return entrenadorRepository.findById(entrenadorId)
                .map(entrenadorMapper::toPerfilDTO)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));
    }

    @Override
    @Transactional
    public EntrenadorPerfilDTO editarPerfil(UUID entrenadorId, EntrenadorEditarPerfilDTO request) {
        Entrenador entrenador = entrenadorRepository.findById(entrenadorId)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        entrenador.setNombre(request.nombre());
        entrenador.setDescripcion(request.descripcion());
        entrenador.setExperienciaAnos(request.experienciaAnos());
        entrenador.setMasters(request.masters());

        Entrenador entrenadorActual = entrenadorRepository.save(entrenador);
        return entrenadorMapper.toPerfilDTO(entrenadorActual);
    }

    @Override
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

    @Override
    public EntrenadorResponseDTO ampliarFormacion(UUID id, AmpliarFormacionDTO request, List<MultipartFile> archivos) {
        Entrenador entrenador = entrenadorRepository.findByIdWithDocumentos(id)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado"));

        List<String> urls = subirCertificaciones(archivos);

        try {
            Entrenador entrenadorActual =  persistenceService.ampliarFormacion(entrenador, request, urls, archivos);

            return entrenadorMapper.toResponse(entrenadorActual);
        } catch (Exception e) {
            log.error("Fallo en persistencia, compensando archivos en S3...");
            urls.forEach(storageService::deleteFile);
            throw e;
        }
    }

    @Override
    @Transactional
    public void solicitarBajaCuenta(UUID entrenadorId) {
        if (!entrenadorRepository.existsById(entrenadorId)) {
            throw new EntityNotFoundException("Entrenador no encontrado.");
        }

        asignacionRepository.desactivarAsignacionesPorEntrenador(entrenadorId);

        usuarioService.suspenderUsuario(entrenadorId);
    }

    private String determinarServicioLabel(List<Asignacion> asigs) {
        if (asigs.size() > 1) return "AMBOS";
        return asigs.get(0).getTipoServicio().name();
    }

    private boolean calcularSiTienePlanActivo(Atleta a, String entrenadorEmail) {
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

    private List<String> subirCertificaciones(List<MultipartFile> certificaciones) {
        return Optional.ofNullable(certificaciones)
                .orElse(List.of())
                .stream()
                .filter(f -> !f.isEmpty())
                .map(storageService::uploadFile)
                .toList();
    }

    private String subirFotoPerfil(MultipartFile foto) {
        return Optional.ofNullable(foto)
                .filter(f -> !f.isEmpty())
                .map(storageService::uploadEntrenadorFoto)
                .orElse(null);
    }

    private void compensarArchivos(List<String> urls, String fotoKey) {
        // Borrado de certificaciones
        urls.forEach(storageService::deleteFile);

        // Borrado de foto solo si llegó a subirse algo
        Optional.ofNullable(fotoKey).ifPresent(storageService::deleteFile);
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

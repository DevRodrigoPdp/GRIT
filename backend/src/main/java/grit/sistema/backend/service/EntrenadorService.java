package grit.sistema.backend.service;

import grit.sistema.backend.dto.entrenador.EntrenadorRequestDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorResponseDTO;
import grit.sistema.backend.exception.UsuarioExistenteException;
import grit.sistema.backend.mapper.EntrenadorMapper;
import grit.sistema.backend.model.Entrenador;
import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import grit.sistema.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrenadorService {
    private final EntrenadorPersistenceService persistenceService;
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
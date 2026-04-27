package grit.sistema.backend.service.common;

import grit.sistema.backend.dto.coaching.DocumentoDTO;
import grit.sistema.backend.dto.coaching.EntrenadorPendienteDTO;
import grit.sistema.backend.entity.coaching.DocumentoEntrenador;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.DocStatus;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class AdminService {
    private final EntrenadorRepository entrenadorRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public Page<EntrenadorPendienteDTO> obtenerPendientes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return entrenadorRepository
                .findByEstadoRevision(EstadoRevision.PENDIENTE_REVISION, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Procesa la decisión del administrador sobre un perfil pendiente.
     */
    @Transactional
    public void procesarAprobacion(UUID id, boolean aprobado, String motivo) {
        Entrenador entrenador = entrenadorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entrenador no encontrado con ID: " + id));

        if (aprobado) {
            aprobarEntrenador(entrenador);
        } else {
            rechazarEntrenador(entrenador, motivo);
        }

        entrenadorRepository.save(entrenador);
        log.info("Entrenador {} procesado. Resultado: {}", id, aprobado ? "APROBADO" : "RECHAZADO");
    }

    /**
     * Elimina completamente a un entrenador del sistema y sus archivos asociados.
     * ¡CUIDADO!: Esta operación es irreversible.
     */
    @Transactional
    public void eliminarEntrenadorDefinitivo(UUID id) {
        Entrenador entrenador = entrenadorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se puede eliminar: Entrenador no encontrado"));

        // 1. Recolectamos las keys de S3 antes de borrar de la DB
        List<String> archivosABorrar = entrenador.getDocumentos().stream()
                .map(DocumentoEntrenador::getUrlS3) // Asumiendo que urlS3 guarda la Key
                .toList();

        // 2. Borramos de la base de datos
        // Al tener CascadeType.ALL en la relación con documentos, se borran automáticamente de la DB
        entrenadorRepository.delete(entrenador);

        // 3. Borramos de S3/MinIO
        // Lo hacemos después del delete de la DB para asegurar que si la DB falla,
        // los archivos sigan ahí para reintentar.
        archivosABorrar.forEach(storageService::deleteFile);

        log.warn("Entrenador {} y sus {} archivos han sido eliminados permanentemente", id, archivosABorrar.size());
    }

    // --- MÉTODOS PRIVADOS DE APOYO (ENCAPSULAMIENTO) ---

    private void aprobarEntrenador(Entrenador entrenador) {
        entrenador.setEstadoRevision(EstadoRevision.APROBADO);
        entrenador.setEstado(EstadoUsuario.ACTIVO);

        entrenador.getDocumentos().forEach(d -> {
            d.setStatus(DocStatus.verified);
            d.setReviewedAt(java.time.OffsetDateTime.now());
        });
    }

    private void rechazarEntrenador(Entrenador entrenador, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio para informar al usuario");
        }

        entrenador.setEstadoRevision(EstadoRevision.RECHAZADO);
        entrenador.setEstado(EstadoUsuario.SUSPENDIDO);

        entrenador.getDocumentos().forEach(d -> {
            d.setStatus(DocStatus.rejected);
            d.setRejectionReason(motivo);
            d.setReviewedAt(java.time.OffsetDateTime.now());
        });
    }

    private EntrenadorPendienteDTO mapToDTO(Entrenador e) {
        var docs = e.getDocumentos().stream()
                .map(d -> new DocumentoDTO(
                        d.getId(),
                        d.getNombreArchivo(),
                        storageService.getPresignedUrl(d.getUrlS3()), // URL de 15 min
                        d.getUploadedAt(),
                        d.getStatus().name()))
                .toList();

        return new EntrenadorPendienteDTO(
                e.getId(),
                e.getNombre(),
                e.getEmail(),
                e.getTitulacionEntrenamiento().name(),
                e.getTitulacionNutricion().name(),
                e.getCodigoProfesional(),
                e.getCreatedAt(),
                docs
        );
    }
}

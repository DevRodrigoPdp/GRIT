package grit.sistema.backend.service.admin;

import grit.sistema.backend.dto.coaching.DocumentoDTO;
import grit.sistema.backend.dto.coaching.EntrenadorPendienteDTO;
import grit.sistema.backend.dto.common.ArchivosAEliminarEventDTO;
import grit.sistema.backend.dto.usuario.UsuarioDTO;
import grit.sistema.backend.dto.usuario.UsuarioResponseDTO;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.DocumentoEntrenador;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.DocStatus;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.mapper.usuario.UsuarioMapper;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.usuario.UsuarioRepository;
import grit.sistema.backend.service.common.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class AdminService {
    private final EntrenadorRepository entrenadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> buscarUsuarios(String termino, Pageable pageable) {
        if (termino == null || termino.isBlank()) {
            return usuarioRepository.findAll(pageable).map(usuarioMapper::toResponseDTO);
        }

        return usuarioRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(
                termino, termino, pageable).map(usuarioMapper::toResponseDTO);
    }

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

    @Transactional
    public void eliminarUsuarioCompleto(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario con ID: " + id));

        List<String> keysParaBorrar = new ArrayList<>();

        if (usuario instanceof Entrenador entrenador) {
            log.info("Identificado como Entrenador. Recolectando documentos y fotos...");

            if (entrenador.getFotoUrl() != null) keysParaBorrar.add(entrenador.getFotoUrl());

            entrenador.getDocumentos().forEach(doc -> {
                if (doc.getUrlS3() != null) keysParaBorrar.add(doc.getUrlS3());
            });

        } else if (usuario instanceof Atleta atleta) {
            log.info("Identificado como Atleta. Recolectando fotos...");

            if (atleta.getFotoUrl() != null) keysParaBorrar.add(atleta.getFotoUrl());

        }

        usuarioRepository.delete(usuario);

        eventPublisher.publishEvent(new ArchivosAEliminarEventDTO(keysParaBorrar));

        log.info("Eliminación definitiva completada para el usuario: {}", id);
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
                        storageService.getPresignedUrl(d.getUrlS3()),
                        d.getUploadedAt(),
                        d.getStatus().name()))
                .toList();

        return new EntrenadorPendienteDTO(
                e.getId(),
                e.getNombre(),
                e.getEmail(),
                e.getTitulacionEntrenamiento() != null ? e.getTitulacionEntrenamiento().name() : "SIN_TITULO",
                e.getTitulacionNutricion() != null ? e.getTitulacionNutricion().name() : "SIN_TITULO",
                e.getCodigoProfesional(),
                e.getCreatedAt(),
                docs
        );
    }
}

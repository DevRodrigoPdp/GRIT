package grit.sistema.backend.service;

import grit.sistema.backend.dto.training.HistorialPesoDTO;
import grit.sistema.backend.dto.training.PesoRequestDTO;
import grit.sistema.backend.dto.training.PesoResponseDTO;
import grit.sistema.backend.dto.training.SolicitudPendienteDTO;
import grit.sistema.backend.exception.BusinessException;
import grit.sistema.backend.entity.training.enums.EstadoCheckin;
import grit.sistema.backend.entity.training.PesoCheckin;
import grit.sistema.backend.entity.training.PesoSolicitud;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.training.PesoCheckinRepository;
import grit.sistema.backend.repository.training.PesoSolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PesoService {

    private final PesoSolicitudRepository solicitudRepository;
    private final PesoCheckinRepository checkinRepository;
    private final AtletaRepository atletaRepository;
    private final EntrenadorRepository entrenadorRepository;

    @Transactional(readOnly = true)
    public SolicitudPendienteDTO obtenerSolicitudPendiente(UUID atletaId) {
        return solicitudRepository.findByAtletaIdAndEstado(atletaId, EstadoCheckin.PENDIENTE)
                .map(s -> new SolicitudPendienteDTO(
                        s.getId(),
                        s.getCreadaEn().toLocalDate(),
                        s.getEntrenador().getNombre() // Asumiendo relación con Entrenador
                ))
                .orElse(null); // El requerimiento pide null si no hay pendiente
    }

    @Transactional
    public PesoResponseDTO registrarPeso(PesoRequestDTO request, UUID atletaId) {
        PesoSolicitud solicitud = solicitudRepository.findById(request.solicitudId())
                .filter(s -> s.getAtleta().getId().equals(atletaId))
                .filter(s -> s.getEstado() == EstadoCheckin.PENDIENTE)
                .orElseThrow(() -> new BusinessException("SOLICITUD_INVALIDA", "La solicitud no existe o ya fue completada."));

        PesoCheckin checkin = new PesoCheckin();
        checkin.setPesoKg(request.pesoKg());
        checkin.setAtleta(solicitud.getAtleta());
        checkin.setSolicitud(solicitud);

        // Corregido: Asegúrate que el enum sea exactamente como se definió
        solicitud.setEstado(EstadoCheckin.COMPLETADA);
        solicitud.setCompletadaEn(LocalDateTime.now());

        // Guardamos el checkin (Hibernate se encarga de persistir el cambio de estado de la solicitud por el dirty checking)
        checkinRepository.save(checkin);

        return new PesoResponseDTO(checkin.getId(), checkin.getFecha(), checkin.getPesoKg());
    }

    @Transactional(readOnly = true)
    public List<HistorialPesoDTO> obtenerHistorialAtleta(UUID atletaId) {
        return checkinRepository.findAllByAtletaIdOrderByFechaAsc(atletaId)
                .stream()
                .map(c -> new HistorialPesoDTO(c.getId(), c.getFecha(), c.getPesoKg(), null))
                .toList();
    }

    // =========================================================================
    // ACCIONES DEL ENTRENADOR
    // =========================================================================

    @Transactional
    public void solicitarCheckin(UUID atletaId, UUID entrenadorId) {
        if (solicitudRepository.existsByAtletaIdAndEstado(atletaId, EstadoCheckin.PENDIENTE)) {
            throw new BusinessException("SOLICITUD_YA_PENDIENTE", "Este atleta ya tiene una solicitud de peso pendiente.");
        }

        PesoSolicitud nuevaSolicitud = new PesoSolicitud();

        // CAMBIO CRÍTICO: getReferenceById evita el SELECT innecesario y el posible null
        nuevaSolicitud.setAtleta(atletaRepository.getReferenceById(atletaId));
        nuevaSolicitud.setEntrenador(entrenadorRepository.getReferenceById(entrenadorId));

        nuevaSolicitud.setEstado(EstadoCheckin.PENDIENTE);
        nuevaSolicitud.setCreadaEn(LocalDateTime.now()); // Siempre asigna explícitamente o usa @PrePersist

        solicitudRepository.save(nuevaSolicitud);
    }

    @Transactional(readOnly = true)
    public boolean tieneSolicitudPendiente(UUID atletaId) {
        return solicitudRepository.existsByAtletaIdAndEstado(atletaId, EstadoCheckin.PENDIENTE);
    }
}
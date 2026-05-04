package grit.sistema.backend.service.training;

import grit.sistema.backend.dto.training.HistorialPesoDTO;
import grit.sistema.backend.dto.training.PesoRequestDTO;
import grit.sistema.backend.dto.training.PesoResponseDTO;
import grit.sistema.backend.dto.training.SolicitudPendienteDTO;

import java.util.List;
import java.util.UUID;

public interface PesoService {

    SolicitudPendienteDTO obtenerSolicitudPendiente(UUID atletaId);

    PesoResponseDTO registrarPeso(PesoRequestDTO request, UUID atletaId);

    List<HistorialPesoDTO> obtenerHistorialAtleta(UUID atletaId);

    void solicitarCheckin(UUID atletaId, UUID entrenadorId);

    boolean tieneSolicitudPendiente(UUID atletaId);
}
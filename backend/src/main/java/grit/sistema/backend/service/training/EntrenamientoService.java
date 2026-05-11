package grit.sistema.backend.service.training;

import grit.sistema.backend.dto.training.*;

import java.util.*;


public interface EntrenamientoService {
    RutinaResponseDTO crearRutina(UUID entrenadorId, RutinaRequestDTO request);
    List<RutinaResponseDTO> listarRutinas(UUID entrenadorId, UUID atletaId);
    RutinaResponseDTO actualizarRutina(UUID entrenadorId, UUID planId,RutinaRequestDTO request);
    void eliminarRutina(UUID entrenadorId, UUID rutinaId);
    Optional<RutinaDTO> getPlanEntrenamientoActivoAtleta(UUID atletaId);
    void activarRutina(UUID entrenadorId, UUID rutinaId);
    void desactivarRutina(UUID entrenadorId, UUID rutinaId);
}

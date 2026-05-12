package grit.sistema.backend.service.coaching;

import grit.sistema.backend.dto.coaching.AsignacionRequestDTO;

import java.util.UUID;

public interface AsignacionService {
    /**
     * Vincula a un atleta con un entrenador mediante un código de invitación.
     * Realiza validaciones de competencia profesional y estado de cuenta.
     */
    void conectarConEntrenador(UUID atletaId, AsignacionRequestDTO request);

    void terminarAsignacion(UUID entrenadorId,UUID atletaId);
}

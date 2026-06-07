package grit.sistema.backend.dto.training;

import java.time.LocalDate;
import java.util.UUID;

public record SolicitudPendienteDTO(
        UUID id,
        LocalDate fecha,
        String solicitadoPor // Nombre del entrenador
){
}

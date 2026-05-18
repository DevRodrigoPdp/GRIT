package grit.sistema.backend.dto.coaching;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface EntrenadorBusquedaDTO {
    // Datos del Usuario (Tabla Padre)
    UUID getId();
    String getNombre();
    String getEmail();
    OffsetDateTime getCreatedAt(); // Aquí estará la fecha que faltaba

    // Datos del Entrenador (Tabla Hijo)
    String getCodigoProfesional();
    String getEstadoRevision();
}

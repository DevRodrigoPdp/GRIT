package grit.sistema.backend.dto.coaching;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EntrenadorBusquedaDTO {
    UUID getId();
    String getNombre();
    String getEmail();
    String getCodigoProfesional();
    String getTitulacionEntrenamiento();
    String getTitulacionNutricion();
    Instant getCreatedAt();
    String getEstadoRevision();
    String getDocumentosRawJson();
}

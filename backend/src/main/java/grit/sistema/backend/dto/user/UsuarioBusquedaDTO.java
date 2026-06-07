package grit.sistema.backend.dto.user;

import java.time.Instant;
import java.util.UUID;

public interface UsuarioBusquedaDTO {
    UUID getId();
    String getNombre();
    String getEmail();
    String getRol(); // El nombre de la columna en la BD
    String getEstado();
    Instant getCreatedAt();
}

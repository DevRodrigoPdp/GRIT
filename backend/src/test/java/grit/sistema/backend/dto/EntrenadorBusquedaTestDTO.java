package grit.sistema.backend.dto;

import grit.sistema.backend.dto.coaching.EntrenadorBusquedaDTO;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EntrenadorBusquedaTestDTO(
        UUID id,
        String nombre,
        String email,
        String estadoRevision,
        String codigoProfesional,
        OffsetDateTime createdAt
) implements EntrenadorBusquedaDTO {
    @Override public UUID getId() { return id; }
    @Override public String getNombre() { return nombre; }
    @Override public String getEmail() { return email; }
    @Override public String getCodigoProfesional() { return codigoProfesional; }
    @Override public String getEstadoRevision() { return estadoRevision; }
    @Override public OffsetDateTime getCreatedAt() { return createdAt; }
}

package grit.sistema.backend.dto;

import grit.sistema.backend.dto.user.UsuarioBusquedaDTO;

import java.util.UUID;

public record UsuarioBusquedaTestDTO(
        UUID id,
        String nombre,
        String email,
        String rol,
        String estado,
        java.time.Instant createdAt
) implements UsuarioBusquedaDTO {
    @Override public UUID getId() { return id; }
    @Override public String getNombre() { return nombre; }
    @Override public String getEmail() { return email; }
    @Override public String getRol() { return rol; }
    @Override public String getEstado() { return estado; }
    @Override public java.time.Instant getCreatedAt() { return createdAt; }
}
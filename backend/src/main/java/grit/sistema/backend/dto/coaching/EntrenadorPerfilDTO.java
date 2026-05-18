package grit.sistema.backend.dto.coaching;

import grit.sistema.backend.entity.coaching.enums.SolicitudAmpliacionTipo;

import java.util.List;
import java.util.UUID;

public record EntrenadorPerfilDTO(
        UUID id,
        String nombre,
        String correo,
        String titulacionEntrenamiento,
        String titulacionNutricion,
        Short experienciaAnos,
        String codigoInvitacion,
        String descripcion,
        String estado,
        boolean tieneAccesoEntrenamiento,
        boolean tieneAccesoNutricion,
        String fotoUrl,
        List<String> masters,
        SolicitudAmpliacionTipo solicitudAmpliacion
) {
}

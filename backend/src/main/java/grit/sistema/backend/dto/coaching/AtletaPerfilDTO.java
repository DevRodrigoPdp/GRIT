package grit.sistema.backend.dto.coaching;

import grit.sistema.backend.entity.coaching.enums.GeneroTipo;
import grit.sistema.backend.entity.coaching.enums.NivelAtleta;
import grit.sistema.backend.entity.coaching.enums.Objetivo;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AtletaPerfilDTO(
        UUID id,
        String nombre,
        String email,
        LocalDate fechaNac,
        GeneroTipo genero,
        BigDecimal peso,
        Integer altura,
        String deporte,
        NivelAtleta nivel,
        TipoServicio servicio,
        Objetivo objetivo
) {
}

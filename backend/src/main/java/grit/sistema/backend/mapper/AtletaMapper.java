package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.atleta.AtletaData;
import grit.sistema.backend.dto.atleta.AtletaRequestDTO;
import grit.sistema.backend.dto.atleta.AtletaResponseDTO;
import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.enums.Objetivo;
import grit.sistema.backend.model.enums.TipoServicio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring") //
public interface AtletaMapper {

    @Mapping(target = "password", ignore = true) //  Nunca mapear password desde un DTO simple
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "objetivo", source = "dto", qualifiedByName = "mapObjetivo")
    Atleta toEntity(AtletaRequestDTO dto);

    @Mapping(target = "ok", constant = "true")
    @Mapping(target = "message", constant = "Perfil creado correctamente.")
    @Mapping(target = "data", source = "atleta")
    AtletaResponseDTO toResponseDTO(Atleta atleta);

    // Mapeo interno para el objeto record AtletaData
    @Mapping(target = "id", source = "id")
    @Mapping(target = "estado", expression = "java(atleta.getEstado().name())")
    @Mapping(target = "rol", expression = "java(atleta.getRol().name())")
    AtletaData toAtletaData(Atleta atleta);

    // Lógica personalizada que tenías en el manual
    @Named("mapObjetivo")
    default Objetivo mapObjetivo(AtletaRequestDTO dto) {
        if (dto.servicio() == TipoServicio.NUTRICION) {
            return null;
        }
        return dto.objetivo();
    }
}

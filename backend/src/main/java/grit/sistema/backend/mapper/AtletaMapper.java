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

@Mapper(componentModel = "spring")
public interface AtletaMapper {

    // 1. De DTO a Entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rol", ignore = true)        // Se asigna en el Service: Rol.ATLETA
    @Mapping(target = "estado", ignore = true)     // Valor por defecto en la Entidad
    // Mapeo de lógica personalizada
    @Mapping(target = "objetivo", source = "dto", qualifiedByName = "mapObjetivo")
    // MapStruct mapeará nombre, email y password automáticamente
    // porque los nombres coinciden entre el Record y la Entidad Usuario.
    Atleta toEntity(AtletaRequestDTO dto);

    // 2. De Entidad a ResponseDTO
    @Mapping(target = "ok", constant = "true")
    @Mapping(target = "message", constant = "Perfil creado correctamente.")
    @Mapping(target = "data", source = "atleta")
    AtletaResponseDTO toResponseDTO(Atleta atleta);

    // 3. Mapeo interno para el Record AtletaData
    @Mapping(target = "id", source = "id")
    @Mapping(target = "estado", expression = "java(atleta.getEstado().name())")
    @Mapping(target = "rol", expression = "java(atleta.getRol().name())")
    AtletaData toAtletaData(Atleta atleta);

    @Named("mapObjetivo")
    default Objetivo mapObjetivo(AtletaRequestDTO dto) {
        if (dto.servicio() == TipoServicio.NUTRICION) {
            return null;
        }
        return dto.objetivo();
    }
}
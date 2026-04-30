package grit.sistema.backend.mapper.coaching;

import grit.sistema.backend.dto.coaching.AtletaData;
import grit.sistema.backend.dto.coaching.AtletaPerfilDTO;
import grit.sistema.backend.dto.coaching.AtletaRequestDTO;
import grit.sistema.backend.dto.coaching.AtletaResponseDTO;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.enums.Objetivo;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.service.common.StorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class AtletaMapper {
    @Autowired
    protected StorageService storageService;

    // 1. De DTO a Entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "objetivo", source = "dto", qualifiedByName = "mapObjetivo")
    public abstract Atleta toEntity(AtletaRequestDTO dto);

    // 2. De Entidad a ResponseDTO
    @Mapping(target = "ok", constant = "true")
    @Mapping(target = "message", constant = "Perfil creado correctamente.")
    @Mapping(target = "data", source = "atleta")
    public abstract AtletaResponseDTO toResponseDTO(Atleta atleta);

    @Mapping(target = "peso", source = "pesoKg")
    @Mapping(target = "altura", source = "alturaCm")
    @Mapping(target = "fotoUrl", source = "fotoUrl", qualifiedByName = "toPresignedUrl")
    public abstract AtletaPerfilDTO toPerfilDTO(Atleta atleta);

    // 3. Mapeo interno para el Record AtletaData
    @Mapping(target = "id", source = "id")
    @Mapping(target = "estado", expression = "java(atleta.getEstado().name())")
    @Mapping(target = "rol", expression = "java(atleta.getRol().name())")
    public abstract AtletaData toAtletaData(Atleta atleta);

    @Named("toPresignedUrl")
    protected String toPresignedUrl(String key) {
        if (key == null || key.isBlank()) return null;
        return storageService.getPresignedUrl(key);
    }

    @Named("mapObjetivo")
    protected Objetivo mapObjetivo(AtletaRequestDTO dto) {
        if (dto.servicio() == TipoServicio.NUTRICION) {
            return null;
        }
        return dto.objetivo();
    }
}
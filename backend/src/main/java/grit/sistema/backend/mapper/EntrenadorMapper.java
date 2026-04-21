package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.entrenador.EntrenadorRequestDTO;
import grit.sistema.backend.dto.entrenador.EntrenadorResponseDTO;
import grit.sistema.backend.model.coaching.DocumentoEntrenador;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.DocStatus;
import grit.sistema.backend.model.enums.EstadoRevision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EntrenadorMapper {

    // Ya NO recibimos el objeto Usuario.
    // Solo el Request (que tiene los datos) y las URLs.
    @Mapping(target = "documentos", source = "urls", qualifiedByName = "mapUrlsToDocumentos")
    @Mapping(target = "estadoRevision", constant = "PENDIENTE_REVISION")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Entrenador toEntity(EntrenadorRequestDTO request, List<String> urls);

    // 2. De Entidad a ResponseDTO (Lectura)
    @Mapping(target = "id", source = "entrenador.id")
    @Mapping(target = "nombre", source = "entrenador.nombre")
    @Mapping(target = "rol", source = "entrenador.rol")
    @Mapping(target = "estado", source = "entrenador.estadoRevision")
    @Mapping(target = "tituloEntrenamiento", source = "entrenador", qualifiedByName = "checkEntrenamiento")
    @Mapping(target = "tituloNutricion", source = "entrenador", qualifiedByName = "checkNutricion")
    EntrenadorResponseDTO toResponse(Entrenador entrenador);

    @Named("checkEntrenamiento")
    default Boolean checkEntrenamiento(Entrenador e) {
        return e.getTitulacionEntrenamiento() != null;
    }

    @Named("checkNutricion")
    default Boolean checkNutricion(Entrenador e) {
        return e.getTitulacionNutricion() != null;
    }

    @Named("mapUrlsToDocumentos")
    default List<DocumentoEntrenador> mapUrlsToDocumentos(List<String> urls) {
        if (urls == null) return List.of();
        return urls.stream().map(url -> {
            DocumentoEntrenador doc = new DocumentoEntrenador();
            doc.setUrlS3(url);
            doc.setNombreArchivo(url.substring(url.lastIndexOf("/") + 1));
            // Asegúrate de que DocStatus coincida con tus enums
            return doc;
        }).toList();
    }
}


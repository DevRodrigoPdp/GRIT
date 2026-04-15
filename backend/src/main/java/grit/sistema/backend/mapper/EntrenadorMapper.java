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

    // 1. De DTO/Usuario a Entidad (Registro)
    @Mapping(target = "id", source = "usuario.id")
    @Mapping(target = "nombre", source = "usuario.nombre")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "rol", source = "usuario.rol")
    @Mapping(target = "password", source = "usuario.password")
    // Campos del Request
    @Mapping(target = "codigoProfesional", source = "request.codigoProfesional")
    @Mapping(target = "titulacionEntrenamiento", source = "request.titulacionEntrenamiento")
    @Mapping(target = "titulacionNutricion", source = "request.titulacionNutricion")
    // Lógica fija
    @Mapping(target = "estadoRevision", constant = "PENDIENTE_REVISION")
    @Mapping(target = "documentos", source = "urls", qualifiedByName = "mapUrlsToDocumentos")
    // Ignorar campos de auditoría y UserDetails
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "estado", ignore = true) // Ignoramos el del padre porque ya viene en el objeto 'usuario'
    Entrenador toEntity(EntrenadorRequestDTO request, Usuario usuario, List<String> urls);

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


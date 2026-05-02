package grit.sistema.backend.mapper.user;

import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.user.UsuarioDTO;
import grit.sistema.backend.dto.user.UsuarioResponseDTO;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Entrenador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    // 1. Mapeo para LoginData
    @Mapping(target = "rol", expression = "java(usuario.getRol().name())")
    @Mapping(target = "estado", expression = "java(usuario.getEstado().name())")
    // 'nombre' se mapea automáticamente si coinciden los nombres en Entity y Record
    @Mapping(target = "tituloEntrenamiento", source = "usuario", qualifiedByName = "mapTituloEnt")
    @Mapping(target = "tituloNutricion", source = "usuario", qualifiedByName = "mapTituloNut")
    @Mapping(target = "servicio", source = "usuario", qualifiedByName = "mapServicio")
    LoginData toLoginData(Usuario usuario);

    // 2. Mapeo a UsuarioDTO (Hacia el Frontend)
    @Mapping(target = "idPublico", source = "id") // CORRECCIÓN: 'id' es el campo de la entidad
    @Mapping(target = "password", ignore = true)
    UsuarioDTO toDTO(Usuario usuario);

    @Mapping(target = "idPublico", source = "id")
    @Mapping(target = "registro", source = "createdAt")
    @Mapping(target = "estado", expression = "java(usuario.getEstado().name())")
    UsuarioResponseDTO toResponseDTO(Usuario usuario);

    // 3. Mapeo hacia Entidad (Desde el Frontend)
    @Mapping(target = "id", ignore = true)        // El ID lo genera la DB
    @Mapping(target = "createdAt", ignore = true) // Auditoría protegida
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "estado", ignore = true)    // El estado se controla en Service
    // Ignorar campos de UserDetails
    Usuario toEntity(UsuarioDTO dto);

    // --- MÉTODOS DE SOPORTE ---
    @Named("mapTituloEnt")
    default Boolean mapTituloEnt(Usuario u) {
        if (u instanceof Entrenador e) return e.getTitulacionEntrenamiento() != null;
        return null;
    }

    @Named("mapTituloNut")
    default Boolean mapTituloNut(Usuario u) {
        if (u instanceof Entrenador e) return e.getTitulacionNutricion() != null;
        return null;
    }

    @Named("mapServicio")
    default String mapServicio(Usuario u) {
        if (u instanceof Atleta a && a.getServicio() != null) {
            return a.getServicio().name();
        }
        return null;
    }
}


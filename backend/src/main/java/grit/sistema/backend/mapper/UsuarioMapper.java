package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.login.LoginData;
import grit.sistema.backend.dto.usuario.UsuarioDTO;
import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.coaching.Entrenador;
import grit.sistema.backend.model.enums.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // 1. Mapeo para el Login (La parte compleja)
    @Mapping(target = "rol", expression = "java(usuario.getRol().name())")
    @Mapping(target = "estado", expression = "java(usuario.getEstado().name())")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "tituloEntrenamiento", source = "usuario", qualifiedByName = "mapTituloEnt")
    @Mapping(target = "tituloNutricion", source = "usuario", qualifiedByName = "mapTituloNut")
    @Mapping(target = "servicio", source = "usuario", qualifiedByName = "mapServicio")
    LoginData toLoginData(Usuario usuario);

    // 2. Mapeo estándar DTO
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", expression = "java(usuario.getId() != null ? usuario.getId().toString() : null)")
    UsuarioDTO toDTO(Usuario usuario);

    // 3. Mapeo hacia Entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Usuario toEntity(UsuarioDTO dto);

    // --- MÉTODOS DE SOPORTE POLIMÓRFICO ---

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

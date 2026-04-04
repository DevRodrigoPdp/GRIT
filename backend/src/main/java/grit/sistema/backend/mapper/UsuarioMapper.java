package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.LoginData;
import grit.sistema.backend.dto.LoginResponseDTO;
import grit.sistema.backend.dto.UsuarioDTO;
import grit.sistema.backend.model.Atleta;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.Rol;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    public LoginData toLoginData(Usuario usuario) {
        // 1. Extraemos lo común de la clase Usuario
        String nombre = usuario.getNombre();
        String rol = usuario.getRol().name();
        String estado = usuario.getEstado().name();

        // 2. Usamos Pattern Matching (Java 17+) para verificar la subclase
        if (usuario instanceof Atleta atleta) {
            // Si es instancia de Atleta, tenemos acceso a .getServicio()
            return new LoginData(
                    rol,
                    estado,
                    nombre,
                    null, // tituloEntrenamiento (Atleta -> null)
                    null, // tituloNutricion (Atleta -> null)
                    atleta.getServicio() != null ? atleta.getServicio().name() : null
            );
        }

//        if (usuario instanceof Entrenador entrenador) {
//            // Si es instancia de Entrenador, tenemos acceso a los títulos
//            return new LoginDataResponse(
//                    rol,
//                    estado,
//                    nombre,
//                    entrenador.getTituloEntrenamiento(),
//                    entrenador.getTituloNutricion(),
//                    null // servicio (Entrenador -> null)
//            );
//        }

        // Caso por defecto (por si tuvieras un Admin puro u otro rol)
        return new LoginData(rol, estado, nombre, null, null, null);
    }

    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;

        return new UsuarioDTO(
                usuario.getUuid() != null ? usuario.getUuid().toString() : null,
                usuario.getNombre(),
                usuario.getEmail(),
                null, // IMPORTANTE: Enviamos null en el password por seguridad
                usuario.getRol() != null ? usuario.getRol().name() : null
        );
    }

    public Usuario toEntity(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) return null;

        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.nombre());
        usuario.setEmail(usuarioDTO.email());

        return usuario;
    }
}

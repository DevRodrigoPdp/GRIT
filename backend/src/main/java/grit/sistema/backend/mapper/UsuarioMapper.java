package grit.sistema.backend.mapper;

import grit.sistema.backend.dto.UsuarioDTO;
import grit.sistema.backend.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;

        return new UsuarioDTO(
                usuario.getUuid() != null ? usuario.getUuid().toString() : null,
                usuario.getNombre(),
                null,
                usuario.getEmail()
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

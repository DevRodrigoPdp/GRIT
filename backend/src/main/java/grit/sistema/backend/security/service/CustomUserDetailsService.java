package grit.sistema.backend.security.service;

import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.repository.usuario.UsuarioRepository;
import grit.sistema.backend.security.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new DisabledException("La cuenta no está activa o ha sido eliminada.");
        }

        return new UserPrincipal(usuario);
    }
}

package grit.sistema.backend.security.model;

import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {
    private final UUID id;
    private final String email;
    private final String password;
    private final boolean activo;
    private final Collection<? extends GrantedAuthority> authorities;

    // Atributos de negocio específicos
    private final boolean tieneTituloNutricion;
    private final boolean tieneTituloEntrenamiento;
    private final TipoServicio servicio;
    private final EstadoUsuario estado;
    private final Rol rol;

    public UserPrincipal(Usuario usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.password = usuario.getPassword();
        this.activo = (usuario.getEstado() == EstadoUsuario.ACTIVO);
        this.estado = usuario.getEstado();
        this.rol = usuario.getRol();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));


        if (usuario instanceof Entrenador entrenador) {
            this.tieneTituloNutricion = entrenador.getTitulacionNutricion() != null;
            this.tieneTituloEntrenamiento = entrenador.getTitulacionEntrenamiento() != null;
            this.servicio = null;
        } else if (usuario instanceof Atleta atleta) {
            this.servicio = atleta.getServicio();
            this.tieneTituloNutricion = false;
            this.tieneTituloEntrenamiento = false;
        }else{
            this.tieneTituloNutricion = false;
            this.tieneTituloEntrenamiento = false;
            this.servicio = null;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isEnabled() {
        return this.estado == EstadoUsuario.ACTIVO;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}


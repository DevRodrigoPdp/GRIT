package grit.sistema.backend.config;

import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.repository.usuario.UsuarioRepository;
import grit.sistema.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UsuarioRepository usuarioRepository;

    @Value("${application.security.pepper}")
    private String pepper;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Usuario usuario = usuarioRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
                throw new DisabledException("La cuenta no está activa o ha sido eliminada.");
            }

            return new UserPrincipal(usuario);
        };
    }
    @Bean
    public AuthenticationProvider  authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        final BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(12);
        // Definimos un costo de 12 para ser más robustos que el default
        // Retornamos una implementación personalizada que aplique el pepper
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bCrypt.encode(rawPassword + pepper);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return bCrypt.matches(rawPassword + pepper, encodedPassword);
            }
        };
    }
}

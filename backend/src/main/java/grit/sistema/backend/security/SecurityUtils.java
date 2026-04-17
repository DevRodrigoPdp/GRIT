package grit.sistema.backend.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("auth")
public class SecurityUtils {

    /**
     * Verifica si el usuario autenticado tiene título de nutrición.
     */
    public boolean tieneTituloNutricion() {
        return getUsuarioActual()
                .map(UserPrincipal::isTieneTituloNutricion)
                .orElse(false);
    }

    /**
     * Verifica si el usuario autenticado tiene título de entrenamiento.
     */
    public boolean tieneTituloEntrenamiento() {
        return getUsuarioActual()
                .map(UserPrincipal::isTieneTituloEntrenamiento)
                .orElse(false);
    }

    /**
     * Extrae el UserPrincipal de forma segura del contexto.
     */
    private Optional<UserPrincipal> getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        // Importante: Spring Security puede devolver un String "anonymousUser"
        // si el endpoint es público. Debemos validar el tipo.
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }

        return Optional.empty();
    }

    /**
     * Método estático de utilidad para usar directamente en código Java (Services).
     */
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal user) {
            return user.getId();
        }
        throw new SecurityException("Operación no permitida: No hay un usuario autenticado en el contexto.");
    }
}

package grit.sistema.backend.repositories;

import grit.sistema.backend.model.Entrenador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntrenadorRepository  extends JpaRepository<Entrenador, UUID> {
    // 1. Verificar si ya existe un código profesional (para el error 409)
    boolean existsByCodigoProfesional(String codigoProfesional);

    // 2. Buscar entrenador por el correo del usuario asociado (muy útil para el login/perfil)
    Optional<Entrenador> findByUsuarioEmail(String usuarioEmail);

    // 3. Verificar si un usuario ya tiene un perfil de entrenador creado
    boolean existsByUsuarioId(UUID usuarioId);
}

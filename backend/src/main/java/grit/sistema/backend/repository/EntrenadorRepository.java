package grit.sistema.backend.repository;

import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;

import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntrenadorRepository  extends JpaRepository<Entrenador, UUID> {
    // 1. Verificar si ya existe un código profesional (para el error 409)
    boolean existsByCodigoProfesional(String codigoProfesional);

    // 2. Buscar entrenador por el correo del usuario asociado (muy útil para el login/perfil)
    Optional<Entrenador> findByEmail(String email);

    Page<Entrenador> findByEstadoRevision(EstadoRevision estado, Pageable pageable);

    boolean existsById(UUID id);

    Optional<Entrenador> findByCodigoInvitacionAndEstado(String codigo, EstadoUsuario activo);

    Optional<Entrenador> findByCodigoInvitacion(String codigo);
}

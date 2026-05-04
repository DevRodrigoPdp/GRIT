package grit.sistema.backend.repository.coaching;

import grit.sistema.backend.dto.coaching.EntrenadorBusquedaDTO;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;

import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
    SELECT
        u.id, u.nombre, u.email, u.created_at,
        e.codigo_profesional, e.estado_revision
    FROM entrenadores e
    INNER JOIN usuarios u ON e.id = u.id
    WHERE e.estado_revision = CAST(:estado AS estado_revision_tipo)
    AND (:search IS NULL OR :search = '' OR
         public.immutable_unaccent(LOWER(u.nombre)) ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :search, '%'))) OR
         LOWER(u.email) ILIKE LOWER(CONCAT('%', :search, '%')))
    ORDER BY
        CASE
            WHEN :search IS NULL OR :search = '' THEN 0
            WHEN LOWER(u.email) = LOWER(:search) THEN 0
            WHEN public.immutable_unaccent(LOWER(u.nombre)) = public.immutable_unaccent(LOWER(:search)) THEN 1
            WHEN public.immutable_unaccent(LOWER(u.nombre)) LIKE public.immutable_unaccent(LOWER(CONCAT(:search, '%'))) THEN 2
            ELSE 3
        END,
        u.created_at ASC,
        u.nombre ASC
    """,
            countQuery = """
        SELECT count(*) FROM entrenadores e
        INNER JOIN usuarios u ON e.id = u.id
        WHERE e.estado_revision = CAST(:estado AS estado_revision_tipo)
        AND (:search IS NULL OR :search = '' OR
             public.immutable_unaccent(LOWER(u.nombre)) ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :search, '%'))) OR
             LOWER(u.email) ILIKE LOWER(CONCAT('%', :search, '%')))
    """,
            nativeQuery = true)
    Page<EntrenadorBusquedaDTO> findPendientesConFiltro(
            @Param("estado") String estado,
            @Param("search") String search,
            Pageable pageable);

    boolean existsById(UUID id);

    Optional<Entrenador> findByCodigoInvitacionAndEstado(String codigo, EstadoUsuario activo);

    Optional<Entrenador> findByCodigoInvitacion(String codigo);
}

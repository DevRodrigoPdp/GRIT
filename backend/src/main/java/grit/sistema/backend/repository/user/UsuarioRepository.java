package grit.sistema.backend.repository.user;

import grit.sistema.backend.dto.user.UsuarioBusquedaDTO;
import grit.sistema.backend.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    @Query(value = """
    SELECT u.id, u.nombre, u.email, u.rol, u.estado, u.created_at
    FROM usuarios u
    WHERE (:termino IS NULL OR :termino = '' OR
           public.immutable_unaccent(LOWER(u.nombre)) ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :termino, '%'))) OR
           LOWER(u.email) ILIKE LOWER(CONCAT('%', :termino, '%')))
    ORDER BY
        CASE
            -- 1. Coincidencia exacta en email
            WHEN LOWER(u.email) = LOWER(:termino) THEN 0
            -- 2. Coincidencia exacta en nombre
            WHEN public.immutable_unaccent(LOWER(u.nombre)) = public.immutable_unaccent(LOWER(:termino)) THEN 1
            -- 3. Empieza por el nombre
            WHEN public.immutable_unaccent(LOWER(u.nombre)) LIKE public.immutable_unaccent(LOWER(CONCAT(:termino, '%'))) THEN 2
            ELSE 3
        END,
        u.nombre ASC
    """,
            countQuery = """
        SELECT count(*) FROM usuarios u
        WHERE (:termino IS NULL OR :termino = '' OR
               public.immutable_unaccent(LOWER(u.nombre)) ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :termino, '%'))) OR
               LOWER(u.email) ILIKE LOWER(CONCAT('%', :termino, '%')))
    """,
            nativeQuery = true)
    Page<UsuarioBusquedaDTO> buscarPorNombreOEmail(@Param("termino") String termino, Pageable pageable);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}

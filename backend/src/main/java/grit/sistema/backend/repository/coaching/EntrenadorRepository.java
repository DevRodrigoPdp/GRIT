package grit.sistema.backend.repository.coaching;

import grit.sistema.backend.dto.coaching.EntrenadorBusquedaDTO;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;
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

    @Query("SELECT e FROM Entrenador e WHERE e.estadoRevision = :estado " +
            "OR e.solicitudAmpliacionPendiente IS NOT NULL")
    Page<Entrenador> findRevisionesPrioritarias(
            @Param("estado") EstadoRevision tipoEstado,
            Pageable pageable
    );

    @Query("SELECT e FROM Entrenador e LEFT JOIN FETCH e.documentos WHERE e.id = :id")
    Optional<Entrenador> findByIdWithDocumentos(@Param("id") UUID id);

    @Query(value = """
        SELECT
            u.id AS id,
            u.nombre AS nombre,
            u.email AS email,
            u.created_at AS createdAt,
            e.codigo_profesional AS codigoProfesional,
            e.titulacion_entrenamiento AS titulacionEntrenamiento,
            e.titulacion_nutricion AS titulacionNutricion,
            COALESCE(
                json_agg(
                    json_build_object(
                        'id', d.id,
                        'nombreArchivo', d.nombre_archivo,
                        'urlFirmada', d.url_s3,
                        'uploadedAt', d.uploaded_at,
                        'status', d.status
                    )
                ) FILTER (WHERE d.id IS NOT NULL), '[]'
            )::text AS documentosRawJson
        FROM entrenadores e
        INNER JOIN usuarios u ON e.id = u.id
        LEFT JOIN documentos_entrenador d ON d.entrenador_id = e.id
        WHERE e.estado_revision = CAST(:estado AS estado_revision_tipo)
        AND (:search IS NULL OR :search = '' OR
             public.immutable_unaccent(LOWER(u.nombre)) ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :search, '%'))) OR
             LOWER(u.email) ILIKE LOWER(CONCAT('%', :search, '%')))
        GROUP BY u.id, u.nombre, u.email, u.created_at, e.codigo_profesional, e.estado_revision, e.titulacion_entrenamiento, e.titulacion_nutricion
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
        SELECT count(DISTINCT e.id)
        FROM entrenadores e
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

    Optional<Entrenador> findByCodigoInvitacion(String codigo);
}

package grit.sistema.backend.repository;

import grit.sistema.backend.model.training.Ejercicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EjercicioRepository extends JpaRepository<Ejercicio, UUID> {

    /**
     * Busca ejercicios cuyo nombre contenga la cadena buscada,
     * ignorando mayúsculas y minúsculas (Ideal para el buscador onInput).
     */
    Page<Ejercicio> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /**
     * Filtra ejercicios que CONTENGAN el grupo muscular (ignorando mayúsculas).
     * Ejemplo: "pier" encontrará "Pierna" y "Tren Inferior".
     */
    Page<Ejercicio> findByGrupoMuscularContainingIgnoreCase(String grupoMuscular, Pageable pageable);

    /**
     * Busca ejercicios que pertenezcan a cualquiera de los grupos enviados.
     * Útil si el usuario selecciona varios filtros en el frontend.
     */
    Page<Ejercicio> findByGrupoMuscularIn(List<String> grupos, Pageable pageable);

    @Query(value = """
    SELECT * FROM ejercicios 
    WHERE grupo_muscular ILIKE %:query% 
    OR nombre ILIKE %:query%
    ORDER BY nombre ASC
    """,
            countQuery = """
    SELECT count(*) FROM ejercicios 
    WHERE grupo_muscular ILIKE %:query% 
    OR nombre ILIKE %:query%
    """,
            nativeQuery = true)
    Page<Ejercicio> buscarFlexible(@Param("query") String query, Pageable pageable);

    /**
     * Combinación: Busca por nombre dentro de un grupo muscular específico.
     */
    Page<Ejercicio> findByNombreContainingIgnoreCaseAndGrupoMuscularContainingIgnoreCase(
            String nombre,
            String grupoMuscular,
            Pageable pageable
    );
}

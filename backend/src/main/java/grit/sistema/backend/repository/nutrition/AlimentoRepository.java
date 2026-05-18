package grit.sistema.backend.repository.nutrition;

import grit.sistema.backend.entity.nutrition.Alimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para acceder a la tabla 'alimentos'.
 * Proporciona búsquedas eficientes por nombre y marca.
 */
@Repository
public interface AlimentoRepository extends JpaRepository<Alimento, UUID> {

    @Query(value = """
        SELECT * FROM alimentos
        WHERE public.immutable_unaccent(LOWER(nombre))    ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :q, '%')))
           OR public.immutable_unaccent(LOWER(marca))     ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :q, '%')))
           OR public.immutable_unaccent(LOWER(categoria)) ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :q, '%')))
        ORDER BY
            CASE
                WHEN public.immutable_unaccent(LOWER(nombre)) = public.immutable_unaccent(LOWER(:q)) THEN 0
                WHEN public.immutable_unaccent(LOWER(nombre)) LIKE public.immutable_unaccent(LOWER(CONCAT(:q, '%'))) THEN 1
                ELSE 2
            END,
            nombre ASC
        """,
            countQuery = """
            SELECT count(*) FROM alimentos
            WHERE public.immutable_unaccent(LOWER(nombre))    ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :q, '%')))
               OR public.immutable_unaccent(LOWER(marca))     ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :q, '%')))
               OR public.immutable_unaccent(LOWER(categoria)) ILIKE public.immutable_unaccent(LOWER(CONCAT('%', :q, '%')))
        """,
            nativeQuery = true)
    Page<Alimento> buscadorGlobal(@Param("q") String q, Pageable pageable);

    @Query("SELECT DISTINCT a.categoria FROM Alimento a ORDER BY a.categoria ASC")
    List<String> obtenerCategoriasUnicas();

    // Búsqueda específica por categoría (Flexible)
    Page<Alimento> findByCategoriaContainingIgnoreCase(String categoria, Pageable pageable);

    // Búsqueda combinada: Nombre/Marca + Categoría
    @Query(value = """
            SELECT * FROM alimentos
            WHERE (nombre ILIKE %:q% OR marca ILIKE %:q%)
              AND categoria ILIKE %:cat%
            ORDER BY nombre ASC
            """,
            countQuery = "SELECT count(*) FROM alimentos WHERE (nombre ILIKE %:q% OR marca ILIKE %:q%) AND categoria ILIKE %:cat%",
            nativeQuery = true)
    Page<Alimento> buscarPorNombreYCategoria(@Param("q") String q, @Param("cat") String cat, Pageable pageable);
}


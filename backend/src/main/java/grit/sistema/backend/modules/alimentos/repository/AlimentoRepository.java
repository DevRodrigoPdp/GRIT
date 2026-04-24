package grit.sistema.backend.modules.alimentos.repository;

import grit.sistema.backend.modules.alimentos.model.Alimento;
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

    // Búsqueda Global (la que ya tenías, mejorada para incluir categoría en el count)
    @Query(value = """
            SELECT * FROM alimentos
            WHERE nombre    ILIKE %:q%
               OR marca     ILIKE %:q%
               OR categoria ILIKE %:q%
            ORDER BY 
                CASE 
                    WHEN LOWER(nombre) = LOWER(:q) THEN 0 
                    WHEN LOWER(nombre) LIKE LOWER(CONCAT(:q, '%')) THEN 1
                    ELSE 2 
                END,
                nombre ASC
            """,
            countQuery = """
                SELECT count(*) FROM alimentos 
                WHERE nombre ILIKE %:q% OR marca ILIKE %:q% OR categoria ILIKE %:q%
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


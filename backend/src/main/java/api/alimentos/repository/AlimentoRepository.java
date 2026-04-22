package api.alimentos.repository;

import api.alimentos.model.Alimento;
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

    /**
     * Busca alimentos por nombre o marca, ordenados por relevancia.
     * Primero devuelve coincidencias exactas, luego parciales.
     * Máximo 15 resultados.
     *
     * @param q Texto a buscar
     * @return Lista de alimentos encontrados (máximo 15)
     */
    @Query(value = """
            SELECT * FROM alimentos
            WHERE LOWER(nombre) ILIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(marca) ILIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY 
                CASE WHEN LOWER(nombre) = LOWER(:q) THEN 0 ELSE 1 END,
                nombre
            LIMIT 15
            """, nativeQuery = true)
    List<Alimento> buscarPorNombreOMarca(@Param("q") String q);

    /**
     * Cuenta cuántos alimentos existen en la tabla.
     * Usado para verificar si la tabla está vacía (para el seeder).
     *
     * @return Número de alimentos en la tabla
     */
    long count();
}

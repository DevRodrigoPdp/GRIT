package grit.sistema.backend.repository.nutrition;

import grit.sistema.backend.entity.nutrition.PlanNutricion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("planRepository")
public interface PlanNutricionRepository extends JpaRepository<PlanNutricion, UUID> {

    List<PlanNutricion> findAllByEntrenadorIdOrderByCreadoEnDesc(UUID entrenadorId);

    List<PlanNutricion> findAllByEntrenadorIdAndAtletaIdOrderByCreadoEnDesc(UUID entrenadorId, UUID atletaId);

    Optional<PlanNutricion> findByIdAndEntrenadorId(UUID id, UUID entrenadorId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PlanNutricion p SET p.activo = false WHERE p.atleta.id = :atletaId AND p.activo = true")
    void desactivarPlanesActivos(@Param("atletaId") UUID atletaId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PlanNutricion p SET p.activo = false WHERE p.entrenador.id = :entrenadorId AND p.atleta.id = :atletaId AND p.activo = true")
    void desactivarPlanesNutricionActivos(@Param("entrenadorId") UUID entrenadorId, @Param("atletaId") UUID atletaId);

    Optional<PlanNutricion> findByAtletaIdAndActivoTrue(UUID atletaId);
}

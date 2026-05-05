package grit.sistema.backend.repository.nutrition;

import grit.sistema.backend.entity.nutrition.PlanNutricion;
import grit.sistema.backend.entity.training.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanNutricionRepository extends JpaRepository<PlanNutricion, UUID> {
    List<PlanNutricion> findAllByEntrenadorId(UUID entrenadorId);
    List<PlanNutricion> findAllByEntrenadorIdAndAtletaId(UUID entrenadorId, UUID atletaId);
    Optional<PlanNutricion> findByIdAndEntrenadorId(UUID id, UUID entrenadorId);

    @Modifying
    @Query("UPDATE PlanNutricion p SET p.activo = false WHERE p.atleta.id = :atletaId AND p.activo = true")
    void desactivarPlanesActivos(@Param("atletaId") UUID atletaId);

    Optional<PlanNutricion> findByAtletaIdAndActivoTrue(UUID atletaId);
}

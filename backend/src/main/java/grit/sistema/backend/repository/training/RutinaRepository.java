package grit.sistema.backend.repository.training;

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
public interface RutinaRepository extends JpaRepository<Rutina, UUID> {
    List<Rutina> findAllByEntrenadorIdOrderByCreadoEnDesc(UUID entrenadorId);
    List<Rutina> findAllByEntrenadorIdAndAtletaIdOrderByCreadoEnDesc(UUID entrenadorId, UUID atletaId);

    Optional<Rutina> findByIdAndEntrenadorId(UUID id, UUID entrenadorId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Rutina r SET r.activo = false WHERE r.atleta.id = :atletaId AND r.activo = true")
    void desactivarRutinasActivas(@Param("atletaId") UUID atletaId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Rutina r SET r.activo = false WHERE r.entrenador.id = :entrenadorId AND r.atleta.id = :atletaId AND r.activo = true")
    void desactivarRutinasActivas(@Param("entrenadorId") UUID entrenadorId, @Param("atletaId") UUID atletaId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Rutina r SET r.activo = false WHERE r.entrenador.id = :entrenadorId AND r.activo = true")
    void desactivarRutinasPorEntrenador(@Param("entrenadorId") UUID entrenadorId);

    Optional<Rutina> findByAtletaIdAndActivoTrue(UUID atletaId);
}

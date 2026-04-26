package grit.sistema.backend.repository;

import grit.sistema.backend.model.coaching.Asignacion;
import grit.sistema.backend.model.enums.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, UUID> {

    List<Asignacion> findAllByEntrenadorEmailAndActivaTrue(String email);

    @Query("SELECT a FROM Asignacion a " +
            "JOIN FETCH a.entrenador e " +
            "WHERE a.atleta.id = :atletaId " +
            "AND a.activa = true")
    List<Asignacion> findAsignacionesActivas(@Param("atletaId") UUID atletaId);

    boolean existsByAtletaIdAndTipoServicioAndActivaTrue(UUID atletaId, TipoServicio tipoServicio);

    boolean existsByAtletaIdAndEntrenadorEmailAndActivaTrue(UUID atletaId, String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Asignacion a SET a.activa = false WHERE a.entrenador.id = :entrenadorId AND a.activa = true")
    void desactivarAsignacionesPorEntrenador(UUID entrenadorId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Asignacion a SET a.activa = false WHERE a.atleta.id = :atletaId AND a.activa = true")
    void desactivarAsignacionesPorAtleta(UUID atletaId);
}

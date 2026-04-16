package grit.sistema.backend.repository;

import grit.sistema.backend.model.coaching.Asignacion;
import grit.sistema.backend.model.enums.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, UUID> {

    List<Asignacion> findAllByEntrenadorEmailAndActivaTrue(String email);

    boolean existsByAtletaIdAndTipoServicioAndActivaTrue(UUID atletaId, TipoServicio tipoServicio);

    boolean existsByAtletaIdAndEntrenadorEmailAndActivaTrue(UUID atletaId, String email);
}

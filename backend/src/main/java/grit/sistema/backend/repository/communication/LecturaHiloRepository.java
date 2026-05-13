package grit.sistema.backend.repository.communication;

import grit.sistema.backend.entity.communication.LecturaHilo;
import grit.sistema.backend.entity.communication.LecturaHiloId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LecturaHiloRepository extends JpaRepository<LecturaHilo, LecturaHiloId> {
}

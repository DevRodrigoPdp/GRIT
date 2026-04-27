package grit.sistema.backend.repository.coaching;

import grit.sistema.backend.entity.coaching.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, UUID> {

    Optional<Atleta> findByEmail(String email);

    boolean existsByEmail(String email);
}

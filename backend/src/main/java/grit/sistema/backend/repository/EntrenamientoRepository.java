package grit.sistema.backend.repository;

import grit.sistema.backend.model.Entrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntrenamientoRepository extends JpaRepository<Entrenamiento, Integer> {
    List<Entrenamiento> findAllByUsuarioEmail(String email);

    Optional<Entrenamiento> findByUuid(UUID uuid);

    @Query("SELECT e FROM Entrenamiento e JOIN FETCH e.usuario WHERE e.uuid = :uuid")
    Optional<Entrenamiento> findByUuidConUsuario(@Param("uuid") UUID uuid);
}

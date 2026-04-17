package grit.sistema.backend.repository;

import grit.sistema.backend.model.nutrition.AlimentoReciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlimentoRecienteRepository extends JpaRepository<AlimentoReciente, UUID> {
    Optional<AlimentoReciente> findByUsuarioIdAndNombreComidaAndCodigoAlimento(UUID usuarioId, String nombreComida, String codigoAlimento);
    List<AlimentoReciente> findTop8ByUsuarioIdAndNombreComidaOrderByUsadoEnDesc(UUID usuarioId, String nombreComida);
    long countByUsuarioIdAndNombreComida(UUID usuarioId, String nombreComida);
}

package grit.sistema.backend.repository.nutrition;

import grit.sistema.backend.entity.nutrition.NotaNutricionista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotaNutricionistaRepository extends JpaRepository<NotaNutricionista, UUID> {

    List<NotaNutricionista> findByAtletaIdOrderByCreadaEnDesc(UUID atletaId);
}
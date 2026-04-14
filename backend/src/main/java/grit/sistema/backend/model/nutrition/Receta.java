package grit.sistema.backend.model.nutrition;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recetas")
@Getter
@Setter
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entrenador_id", nullable = false)
    private UUID entrenadorId;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "gramos_total", precision = 7, scale = 2, nullable = false)
    private BigDecimal gramosTotal;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IngredienteReceta> ingredientes;
}

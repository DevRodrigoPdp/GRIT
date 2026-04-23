package grit.sistema.backend.model.nutrition;


import grit.sistema.backend.model.coaching.Atleta;
import grit.sistema.backend.model.coaching.Entrenador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "planes_nutricion")
@Getter
@Setter
@NoArgsConstructor
public class PlanNutricion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // En PlanNutricion.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    private Entrenador entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comida> comidas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }

    // --- MÉTODOS DE CONVENIENCIA ---

    public void addComida(Comida comida) {
        comidas.add(comida);
        comida.setPlan(this);
    }

    public void removeComida(Comida comida) {
        comidas.remove(comida);
        comida.setPlan(null);
    }
}

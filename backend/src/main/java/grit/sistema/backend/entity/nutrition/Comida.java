package grit.sistema.backend.entity.nutrition;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comidas")
@Getter
@Setter
public class Comida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanNutricion plan;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Short orden;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @OneToMany(mappedBy = "comida",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<AlimentoEnComida> alimentos = new ArrayList<>();


    /**
     * Helper para añadir un alimento asegurando que la FK se asigne correctamente.
     */
    public void addAlimento(AlimentoEnComida alimento) {
        alimentos.add(alimento);
        alimento.setComida(this);
    }

    public void removeAlimento(AlimentoEnComida alimento) {
        alimentos.remove(alimento);
        alimento.setComida(null);
    }
}

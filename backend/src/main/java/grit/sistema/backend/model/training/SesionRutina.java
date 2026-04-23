package grit.sistema.backend.model.training;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sesiones_rutina")
@Getter
@Setter
@NoArgsConstructor
public class SesionRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", nullable = false)
    private Rutina rutina;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Short orden;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<EjercicioEnSesion> ejercicios = new ArrayList<>();

    public void addEjercicio(EjercicioEnSesion ejercicio) {
        ejercicios.add(ejercicio);
        ejercicio.setSesion(this);
    }

    public void removeEjercicio(EjercicioEnSesion ejercicio) {
        ejercicios.remove(ejercicio);
        ejercicio.setSesion(null);
    }
}


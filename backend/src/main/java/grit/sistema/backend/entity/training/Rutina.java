package grit.sistema.backend.entity.training;

import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rutinas")
@Getter
@Setter
@NoArgsConstructor
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @Column(nullable = false)
    private boolean activo = false;

    @Column(name = "creado_en", updatable = false, nullable = false)
    private OffsetDateTime creadoEn;

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<SesionRutina> sesiones = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }

    // --- MÉTODOS DE SINCRONIZACIÓN ---

    public void addSesion(SesionRutina sesion) {
        sesiones.add(sesion);
        sesion.setRutina(this);
    }

    public void removeSesion(SesionRutina sesion) {
        sesiones.remove(sesion);
        sesion.setRutina(null);
    }
}

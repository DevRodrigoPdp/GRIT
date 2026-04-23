package grit.sistema.backend.model.training;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ejercicios_en_sesion")
@Getter
@Setter
public class EjercicioEnSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionRutina sesion;

    @Column(name = "ejercicio_id", nullable = false, length = 100)
    private String ejercicioId;

    @Column(name = "ejercicio_nombre", nullable = false)
    private String ejercicioNombre;

    @Column(name = "ejercicio_categoria", length = 100)
    private String ejercicioCategoria;

    @Column(name = "ejercicio_musculo_principal", length = 100)
    private String ejercicioMusculoPrincipal;

    @Column(name = "ejercicio_imagen_url", columnDefinition = "TEXT")
    private String ejercicioImagenUrl;

    @Column(nullable = false)
    private Short series;

    @Column(nullable = false, length = 50)
    private String reps;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(nullable = false)
    private Short orden;
}

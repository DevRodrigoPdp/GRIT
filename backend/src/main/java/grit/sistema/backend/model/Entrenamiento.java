package grit.sistema.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entrenamientos")
@Getter
@Setter
public class Entrenamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true, nullable = false)
    private UUID uuid = UUID.randomUUID();

    private String nombre;
    private String descripcion;
    private LocalDateTime fechaEntrenamiento;
    private Integer duracionMinutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, referencedColumnName = "id")
    private Usuario usuario;

    @OneToMany(mappedBy = "entrenamiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Ejercicio> ejercicios = new ArrayList<>();
}

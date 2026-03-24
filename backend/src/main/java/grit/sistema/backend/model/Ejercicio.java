package grit.sistema.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ejercicios")
@Getter
@Setter
public class Ejercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private UUID uuid = UUID.randomUUID();

    private String nombre;
    private Integer series;
    private Integer repeticiones;
    private Double pesoKg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenamiento_id", nullable = false)
    private Entrenamiento entrenamiento;
}

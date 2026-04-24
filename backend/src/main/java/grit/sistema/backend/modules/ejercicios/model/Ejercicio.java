package grit.sistema.backend.modules.ejercicios.model;

import grit.sistema.backend.modules.ejercicios.model.enums.Dificultad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ejercicios")
@Getter
@Setter
@NoArgsConstructor
public class Ejercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "grupo_muscular", nullable = false)
    private String grupoMuscular; // [cite: 16]

    @Column(name = "equipo_necesario")
    private String equipoNecesario;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private Dificultad dificultad;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}

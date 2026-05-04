package grit.sistema.backend.entity.communication;

import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.communication.enums.CategoriaHilo;
import grit.sistema.backend.entity.communication.enums.ContextoHilo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "hilos_comunicacion")
@Getter
@Setter
public class Hilo {
    @Id
    private UUID id; // Asignado manualmente o vía generador

    private String titulo;

    @Enumerated(EnumType.STRING)
    private CategoriaHilo categoria;

    @Enumerated(EnumType.STRING)
    private ContextoHilo contexto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id")
    private Atleta atleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id")
    private Entrenador entrenador;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @OneToMany(mappedBy = "hilo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> mensajes = new ArrayList<>();

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    // Dentro de la clase Hilo
    public void addMensaje(Mensaje mensaje) {
        this.mensajes.add(mensaje);
        mensaje.setHilo(this); // Mantiene la consistencia bidireccional
    }
}
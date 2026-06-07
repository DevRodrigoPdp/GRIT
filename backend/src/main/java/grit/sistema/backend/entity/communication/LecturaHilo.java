package grit.sistema.backend.entity.communication;

import grit.sistema.backend.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecturas_hilo")
@IdClass(LecturaHiloId.class)
@Getter
@Setter
@NoArgsConstructor
public class LecturaHilo {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hilo_id")
    private Hilo hilo;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "leido_en", nullable = false)
    private LocalDateTime leidoEn = LocalDateTime.now();

    public LecturaHilo(Hilo hilo, Usuario usuario) {
        this.hilo = hilo;
        this.usuario = usuario;
        this.leidoEn = LocalDateTime.now();
    }
}

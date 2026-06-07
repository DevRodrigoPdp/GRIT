package grit.sistema.backend.entity.communication;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LecturaHiloId implements Serializable {
    private UUID hilo;
    private UUID usuario;
}

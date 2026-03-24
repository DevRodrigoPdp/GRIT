package grit.sistema.backend.model;

import grit.sistema.backend.model.enums.Rol;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid",unique = true, nullable = false)
    @Generated(event = EventType.INSERT)
    private UUID uuid;

    @Column(name = "username")
    private String nombre;
    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.USER;
}

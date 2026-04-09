package grit.sistema.backend.Initializer;

import grit.sistema.backend.model.Atleta;
import grit.sistema.backend.model.Entrenador;
import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.*;
import grit.sistema.backend.repositories.AtletaRepository;
import grit.sistema.backend.repositories.EntrenadorRepository;
import grit.sistema.backend.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("dev") // Solo se ejecuta en modo desarrollo
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.pepper}")
    private String pepper;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           EntrenadorRepository entrenadorRepository,
                           AtletaRepository atletaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.atletaRepository = atletaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            log.info("Base de datos vacía. Creando usuarios iniciales...");
            // 1. Crear Admin primero (independiente)
            crearAdminSiNoExiste();
            // 2. Forzar envío a DB para limpiar el contexto
            usuarioRepository.flush();

            crearEntrenadorSiNoExiste();
            usuarioRepository.flush();

            crearAtletaSiNoExiste();
        } else {
            log.info("La base de datos ya tiene datos. Saltando inicialización.");
        }
    }

    private void crearAdminSiNoExiste() {
        String email = "admin@test.com";
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario user = new Usuario();
            user.setNombre("Admin de Prueba");
            user.setEmail(email);
            user.setRol(Rol.ADMIN);
            user.setEstado(EstadoUsuario.ACTIVO);
            user.setPassword(passwordEncoder.encode("password123" + pepper));

            usuarioRepository.save(user);
            System.out.println(">>>>Usuario Admin creado: admin@test.com / password123");
        }
    }

    private void crearEntrenadorSiNoExiste() {
        String email = "coach@test.com";
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario user = new Usuario();
            user.setNombre("Coach de Prueba");
            user.setEmail(email);
            user.setRol(Rol.ENTRENADOR);
            user.setEstado(EstadoUsuario.ACTIVO);
            user.setPassword(passwordEncoder.encode("password123" + pepper));

            user = usuarioRepository.saveAndFlush(user);

            Entrenador coach = new Entrenador();
            coach.setUsuario(user); // @MapsId tomará el ID de aquí automáticamente
            coach.setId(user.getId()); // Reforzamos el ID
            coach.setCodigoProfesional("COL-00000");
            coach.setTitulacionEntrenamiento(TitulacionEntrenamiento.GRADO_CAFYD);
            coach.setEstado(EstadoRevision.APROBADO);
            coach.setFechaSolicitud(LocalDateTime.now());
            coach.setTieneTituloEntrenamiento(true);

            entrenadorRepository.save(coach);
            System.out.println(">>>>Usuario Entrenador creado: coach@test.com / password123");
        }
    }

    private void crearAtletaSiNoExiste() {
        String email = "atleta@test.com";
        if (!usuarioRepository.existsByEmail(email)) {
            // IMPORTANTE: Instanciamos directamente el HIJO
            Atleta atleta = new Atleta();

            // Seteamos los campos del PADRE (Usuario)
            atleta.setNombre("Atleta de Prueba");
            atleta.setEmail(email);
            atleta.setRol(Rol.ATLETA);
            atleta.setEstado(EstadoUsuario.ACTIVO);
            atleta.setPassword(passwordEncoder.encode("password123" + pepper));

            // Seteamos los campos del HIJO (Atleta)
            atleta.setPesoKg(80.0);
            atleta.setAlturaCm(180);
            atleta.setDeporte("Gimnasio");
            atleta.setObjetivo(Objetivo.PERDER_PESO);
            atleta.setServicio(TipoServicio.AMBOS);
            atleta.setGenero("hombre");
            atleta.setFechaNac(LocalDate.of(1990, 1, 1));
            atleta.setNivel(NivelAtleta.INTERMEDIO);

            // Guardamos UNA sola vez usando el repositorio del hijo (o el del padre, JPA lo entiende)
            atletaRepository.save(atleta);

            System.out.println(">>>>Usuario Atleta creado: atleta@test.com / password123");
        }
    }
}

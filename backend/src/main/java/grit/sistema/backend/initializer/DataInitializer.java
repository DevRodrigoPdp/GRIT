package grit.sistema.backend.initializer;

import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.enums.*;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.repository.AtletaRepository;
import grit.sistema.backend.repository.EntrenadorRepository;
import grit.sistema.backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

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
            Usuario admin = new Usuario();
            admin.setNombre("Admin de Prueba");
            admin.setEmail(email);
            admin.setRol(Rol.ADMIN);
            admin.setEstado(EstadoUsuario.ACTIVO);
            admin.setPassword(passwordEncoder.encode("password123" + pepper));
            usuarioRepository.save(admin);
        }
    }

    private void crearEntrenadorSiNoExiste() {
        String email = "coach@test.com";
        if (!usuarioRepository.existsByEmail(email)) {
            // CORRECCIÓN: Instanciamos al hijo directamente
            Entrenador coach = new Entrenador();

            // Campos del PADRE (Heredados)
            coach.setNombre("Coach de Prueba");
            coach.setEmail(email);
            coach.setRol(Rol.ENTRENADOR);
            coach.setEstado(EstadoUsuario.ACTIVO); // Estado de cuenta
            coach.setPassword(passwordEncoder.encode("password123" + pepper));

            // Campos del HIJO (Específicos de Entrenador)
            coach.setCodigoProfesional("COL-00000");
            coach.setTitulacionEntrenamiento(TitulacionEntrenamiento.GRADO_CAFYD);

            // CORRECCIÓN: Usamos el nuevo nombre del campo de negocio
            coach.setEstadoRevision(EstadoRevision.APROBADO);

            // Eliminamos setFechaSolicitud y setTieneTituloEntrenamiento
            // ya que no están en tu entidad física actual.

            entrenadorRepository.save(coach);
            log.info(">>>> Usuario Entrenador creado: coach@test.com");
        }
    }

    private void crearAtletaSiNoExiste() {
        String email = "atleta@test.com";
        if (!usuarioRepository.existsByEmail(email)) {
            Atleta atleta = new Atleta();

            // Campos PADRE
            atleta.setNombre("Atleta de Prueba");
            atleta.setEmail(email);
            atleta.setRol(Rol.ATLETA);
            atleta.setEstado(EstadoUsuario.ACTIVO);
            atleta.setPassword(passwordEncoder.encode("password123" + pepper));

            // Campos HIJO
            atleta.setPesoKg(new BigDecimal("80.0"));
            atleta.setAlturaCm(180);
            atleta.setDeporte("Gimnasio");
            atleta.setObjetivo(Objetivo.PERDER_PESO);
            atleta.setServicio(TipoServicio.AMBOS);

            // CORRECCIÓN: Usar el Enum GeneroTipo en lugar de String
            atleta.setGenero(GeneroTipo.HOMBRE);

            atleta.setFechaNac(LocalDate.of(1990, 1, 1));
            atleta.setNivel(NivelAtleta.INTERMEDIO);

            atletaRepository.save(atleta);
            log.info(">>>> Usuario Atleta creado: atleta@test.com");
        }
    }
}

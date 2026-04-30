package grit.sistema.backend.initializer;

import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.enums.*;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.usuario.UsuarioRepository;
import grit.sistema.backend.util.CodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile({"dev", "docker"}) // Se activa en ambos entornos
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeGenerator codeGenerator;

    @Value("${app.seed.admin-email:admin@test.com}")
    private String adminEmail;

    @Value("${app.seed.coach-email:coach@test.com}")
    private String coachEmail;

    @Value("${app.seed.athlete-email:atleta@test.com}")
    private String athleteEmail;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           EntrenadorRepository entrenadorRepository,
                           AtletaRepository atletaRepository,
                           PasswordEncoder passwordEncoder, CodeGenerator codeGenerator) {
        this.usuarioRepository = usuarioRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.atletaRepository = atletaRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            log.info(">>>> [SEED] Base de datos vacía. Iniciando creación de usuarios para entorno: {}",
                    System.getProperty("spring.profiles.active"));

            crearAdminSiNoExiste(adminEmail);
            usuarioRepository.flush();

            crearEntrenadorSiNoExiste(coachEmail);
            usuarioRepository.flush();

            crearAtletaSiNoExiste(athleteEmail);

            log.info(">>>> [SEED] Inicialización completada exitosamente.");
        } else {
            log.info(">>>> [SEED] La base de datos ya contiene datos. Saltando inicialización.");
        }
    }

    private void crearAdminSiNoExiste(String email) {
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador Sistema");
            admin.setEmail(email);
            admin.setRol(Rol.ADMIN);
            admin.setEstado(EstadoUsuario.ACTIVO);
            admin.setPassword(passwordEncoder.encode("password123"));
            usuarioRepository.save(admin);
            log.info("Admin creado: {}", email);
        }
    }

    private void crearEntrenadorSiNoExiste(String email) {
        if (!usuarioRepository.existsByEmail(email)) {
            Entrenador coach = new Entrenador();
            // Datos del Padre
            coach.setNombre("Entrenador de Prueba");
            coach.setEmail(email);
            coach.setRol(Rol.ENTRENADOR);
            coach.setEstado(EstadoUsuario.ACTIVO);
            coach.setPassword(passwordEncoder.encode("password123"));

            // Datos del Hijo (Entrenador)
            coach.setCodigoProfesional("COL-00000");
            coach.setTitulacionEntrenamiento(TitulacionEntrenamiento.GRADO_CAFYD);
            coach.setTitulacionNutricion(TitulacionNutricion.GRADO_NUTRICION_DIETETICA);
            coach.setTieneAccesoEntrenamiento(true);
            coach.setTieneAccesoNutricion(true);
            coach.setEstadoRevision(EstadoRevision.APROBADO);
            String nuevoCodigo = codeGenerator.generateGritFormat();
            coach.setCodigoInvitacion(nuevoCodigo);
            coach.setMasters(List.of("Master en Ciencias del Deporte"));

            entrenadorRepository.save(coach);
            log.info("Coach creado: {}", email);
        }
    }

    private void crearAtletaSiNoExiste(String email) {
        if (!usuarioRepository.existsByEmail(email)) {
            Atleta atleta = new Atleta();
            // Datos del Padre
            atleta.setNombre("Atleta de Prueba");
            atleta.setEmail(email);
            atleta.setRol(Rol.ATLETA);
            atleta.setEstado(EstadoUsuario.ACTIVO);
            atleta.setPassword(passwordEncoder.encode("password123"));

            // Datos del Hijo (Atleta)
            atleta.setPesoKg(new BigDecimal("80.0"));
            atleta.setAlturaCm(180);
            atleta.setDeporte("Gimnasio");
            atleta.setObjetivo(Objetivo.PERDER_PESO);
            atleta.setServicio(TipoServicio.AMBOS);
            atleta.setGenero(GeneroTipo.HOMBRE);
            atleta.setFechaNac(LocalDate.of(1990, 1, 1));
            atleta.setNivel(NivelAtleta.INTERMEDIO);
            atleta.setIntolerancias(List.of("Leche"));
            atleta.setAlergias(List.of("Frutos Secos"));

            atletaRepository.save(atleta);
            log.info("Atleta creado: {}", email);
        }
    }
}
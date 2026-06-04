package grit.sistema.backend.initializer;

import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.Atleta;
import grit.sistema.backend.entity.coaching.Entrenador;
import grit.sistema.backend.entity.coaching.enums.*;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.repository.coaching.AtletaRepository;
import grit.sistema.backend.repository.coaching.EntrenadorRepository;
import grit.sistema.backend.repository.user.UsuarioRepository;
import grit.sistema.backend.util.CodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
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

    private final Environment environment;
    private final UsuarioRepository usuarioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeGenerator codeGenerator;

    @Value("${application.seed.admin-email:admin@test.com}")
    private String adminEmail;

    @Value("${application.seed.coach-email:coach@test.com}")
    private String coachEmail;

    @Value("${application.seed.atleta-email:atleta@test.com}")
    private String athleteEmail;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           EntrenadorRepository entrenadorRepository,
                           AtletaRepository atletaRepository,
                           PasswordEncoder passwordEncoder,
                           CodeGenerator codeGenerator,
                           Environment environment) {
        this.usuarioRepository = usuarioRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.atletaRepository = atletaRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeGenerator = codeGenerator;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String perfilesActivos = String.join(", ", environment.getActiveProfiles());

        log.info(">>>> [SEED] Iniciando verificación de usuarios de prueba para el entorno: [{}]", perfilesActivos);

        boolean seHicieronCambios = false;

        if (!usuarioRepository.existsByEmail(adminEmail)) {
            log.info(">>>> [SEED] Creando usuario ADMINISTRADOR: {}", adminEmail);
            crearAdminSiNoExiste(adminEmail);
            seHicieronCambios = true;
        }

        if (!usuarioRepository.existsByEmail(coachEmail)) {
            log.info(">>>> [SEED] Creando usuario ENTRENADOR: {}", coachEmail);
            crearEntrenadorSiNoExiste(coachEmail);
            seHicieronCambios = true;
        }

        if (!usuarioRepository.existsByEmail(athleteEmail)) {
            log.info(">>>> [SEED] Creando usuario ATLETA: {}", athleteEmail);
            crearAtletaSiNoExiste(athleteEmail);
            seHicieronCambios = true;
        }

        if (seHicieronCambios) {
            // Un único flush al final de la transacción optimiza el rendimiento del pool
            usuarioRepository.flush();
            log.info(">>>> [SEED] Inicialización de usuarios completada exitosamente.");
        } else {
            log.info(">>>> [SEED] Todos los usuarios específicos del entorno ya existen. Saltando inicialización.");
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

            String codigoProf = "COL-" + codeGenerator.generateGritFormat().substring(0, 5).toUpperCase();
            coach.setCodigoProfesional(codigoProf);
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
            atleta.setRestriccionesDieteticas("Alergia severa al polén.");
            atleta.setRestriccionesFisicas("Lesión en el 2022 en el peroné.");

            atletaRepository.save(atleta);
            log.info("Atleta creado: {}", email);
        }
    }
}
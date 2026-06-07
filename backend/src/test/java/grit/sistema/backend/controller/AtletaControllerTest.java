package grit.sistema.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.coaching.*;
import grit.sistema.backend.dto.nutrition.PlanData;
import grit.sistema.backend.dto.nutrition.PlanNutricionActivoResponseDTO;
import grit.sistema.backend.dto.training.*;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.enums.GeneroTipo;
import grit.sistema.backend.entity.coaching.enums.Objetivo;
import grit.sistema.backend.entity.coaching.enums.TipoServicio;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.security.jwt.JwtUtils;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.coaching.AsignacionService;
import grit.sistema.backend.service.coaching.AtletaService;
import grit.sistema.backend.service.nutrition.NutricionService;
import grit.sistema.backend.service.training.EntrenamientoService;
import grit.sistema.backend.service.training.PesoService;
import grit.sistema.backend.service.user.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas Controlador de Atletas (AtletaController)")
class AtletaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private UsuarioService usuarioService;
    @MockBean private AtletaService atletaService;
    @MockBean private NutricionService nutricionService;
    @MockBean private EntrenamientoService entrenamientoService;
    @MockBean private AsignacionService asignacionService;
    @MockBean private PesoService pesoService;
    @MockBean private JwtUtils jwtUtils;

    private UUID mockAtletaId;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        mockAtletaId = UUID.randomUUID();

        // Instanciamos un usuario simulado en memoria con datos consistentes
        Usuario usuarioTest = new Usuario();
        usuarioTest.setId(mockAtletaId);
        usuarioTest.setNombre("Juan Atleta");
        usuarioTest.setEmail("juan@atleta.com");
        usuarioTest.setRol(Rol.ATLETA);
        usuarioTest.setEstado(EstadoUsuario.ACTIVO);
        usuarioTest.setPassword("hashed_password");

        principal = new UserPrincipal(usuarioTest);
    }

    // ==========================================
    // SECCIÓN: SEGURIDAD Y ACCESO
    // ==========================================
    @Nested
    @DisplayName("Validación de Seguridad")
    class SecurityTests {

        @Test
        @DisplayName("Debe rechazar con 401 Unauthorized si falta el token de sesión")
        void sinAutenticacion_Retorna401() throws Exception {
            mockMvc.perform(get("/api/v1/atleta/perfil"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==========================================
    // SECCIÓN: PERFIL Y ARCHIVOS
    // ==========================================
    @Nested
    @DisplayName("Endpoints de Gestión de Perfil")
    class PerfilTests {

        @Test
        @DisplayName("Debe obtener el perfil del atleta con directivas Cache-Control")
        void getPerfil_Exito() throws Exception {
            // Instanciación exacta respetando los tipos de tu record AtletaPerfilDTO
            AtletaPerfilDTO perfil = new AtletaPerfilDTO(
                    mockAtletaId,
                    "Juan Atleta",
                    "juan@atleta.com",
                    LocalDate.of(2000, 1, 1),
                    GeneroTipo.HOMBRE, // Enums reales
                    new java.math.BigDecimal("82.50"),                              // BigDecimal preciso
                    180,                                                             // Integer altura
                    "CYCLING",
                    grit.sistema.backend.entity.coaching.enums.NivelAtleta.PRINCIPIANTE,
                    grit.sistema.backend.entity.coaching.enums.TipoServicio.ENTRENAMIENTO,
                    Objetivo.PERDER_PESO,
                    "Sin restricciones",
                    "Ninguna",
                    "http://s3.storage/avatar.png"
            );

            when(atletaService.obtenerPerfil(mockAtletaId)).thenReturn(perfil);

            mockMvc.perform(get("/api/v1/atleta/perfil").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("max-age=30")))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("private")))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("must-revalidate")))
                    .andExpect(jsonPath("$.ok").value(true))
                    .andExpect(jsonPath("$.data.id").value(mockAtletaId.toString()))
                    .andExpect(jsonPath("$.data.nombre").value("Juan Atleta"))
                    .andExpect(jsonPath("$.data.peso").value(82.50))
                    .andExpect(jsonPath("$.data.nivel").value("PRINCIPIANTE"));
        }

        @Test
        @DisplayName("Debe actualizar los datos del perfil")
        void actualizarPerfil_Exito() throws Exception {
            AtletaEditarPerfilDTO editDto = new AtletaEditarPerfilDTO("Juan Modificado", 110, "INTERMEDIO", null,80, "", "");

            // Reutilizamos el record real mapeando los cambios esperados tras la edición
            AtletaPerfilDTO perfilEditado = new AtletaPerfilDTO(
                    mockAtletaId,
                    "Juan Modificado", // Campo editado
                    "juan@atleta.com",
                    LocalDate.of(2000, 1, 1),
                    GeneroTipo.HOMBRE,
                    new java.math.BigDecimal("82.50"),
                    180,
                    "CYCLING",
                    grit.sistema.backend.entity.coaching.enums.NivelAtleta.PRINCIPIANTE,
                    grit.sistema.backend.entity.coaching.enums.TipoServicio.ENTRENAMIENTO,
                    Objetivo.PERDER_PESO,
                    "Sin restricciones",
                    "Ninguna",
                    "http://s3.storage/avatar.png"
            );

            when(atletaService.editarPerfil(eq(mockAtletaId), any(AtletaEditarPerfilDTO.class))).thenReturn(perfilEditado);

            mockMvc.perform(put("/api/v1/atleta/perfil")
                            .with(user(principal)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.nombre").value("Juan Modificado"))
                    .andExpect(jsonPath("$.data.email").value("juan@atleta.com"));
        }

        @Test
        @DisplayName("Debe actualizar la foto de perfil mediante Multipart")
        void actualizarFoto_Exito() throws Exception {
            MockMultipartFile fotoPart = new MockMultipartFile("fotoPerfil", "avatar.png", MediaType.IMAGE_PNG_VALUE, "FOTO_STREAM".getBytes());
            FotoPerfilResponseDTO responseDto = new FotoPerfilResponseDTO("http://s3.storage/avatar.png");

            when(usuarioService.actualizarFotoPerfil(eq(mockAtletaId), any())).thenReturn(responseDto);

            mockMvc.perform(multipart("/api/v1/atleta/foto")
                            .file(fotoPart)
                            .with(user(principal)).with(csrf())
                            .with(request -> { request.setMethod("PATCH"); return request; }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.url").value("http://s3.storage/avatar.png"));
        }
    }

    // ==========================================
    // SECCIÓN: PLANES (ENTRENAMIENTO / NUTRICIÓN)
    // ==========================================
    @Nested
    @DisplayName("Endpoints de Planes Activos")
    class PlanesTests {

        @Test
        @DisplayName("Debe retornar la rutina activa cuando exista")
        void getPlanEntrenamientoActivo_Existe_Exito() throws Exception {
            RutinaDTO rutina = new RutinaDTO(UUID.randomUUID(),UUID.randomUUID(), "Rutina Hipertrofia","rutina buena", OffsetDateTime.now(), Collections.emptyList());
            when(entrenamientoService.getPlanEntrenamientoActivoAtleta(mockAtletaId)).thenReturn(Optional.of(rutina));

            mockMvc.perform(get("/api/v1/atleta/entrenamiento/plan-activo").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.nombre").value("Rutina Hipertrofia"));
        }

        @Test
        @DisplayName("Debe retornar data nula si el Optional de la rutina está vacío")
        void getPlanEntrenamientoActivo_Vacio_Exito() throws Exception {
            when(entrenamientoService.getPlanEntrenamientoActivoAtleta(mockAtletaId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/atleta/entrenamiento/plan-activo").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Debe obtener el plan de nutrición activo")
        void getPlanNutricionActivo_Exito() throws Exception {
            PlanNutricionActivoResponseDTO planNutricion = new PlanNutricionActivoResponseDTO(true, new PlanData(UUID.randomUUID(), "Plan Volumen","fjsdfla", 3000, 180.0, 70.0,30.0, Collections.emptyList()));
            when(nutricionService.getPlanNutricionActivoAtleta(mockAtletaId)).thenReturn(planNutricion);

            mockMvc.perform(get("/api/v1/atleta/nutricion/plan-activo").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.nombre").value("Plan Volumen"));
        }
    }

    // ==========================================
    // SECCIÓN: RELACIONES Y PROFESIONALES
    // ==========================================
    @Nested
    @DisplayName("Endpoints de Asignaciones y Conexión")
    class AsignacionTests {

        @Test
        @DisplayName("Debe listar los profesionales asignados al atleta")
        void getProfesionalesAsignados_Exito() throws Exception {
            ProfesionalAsignadoDTO prof = new ProfesionalAsignadoDTO(UUID.randomUUID(), "Kevin Garcia", "GRADO ENTRENAMIENTO", "ENTRENADOR", "lasjfdkasjf");
            when(atletaService.getProfesionalesAsignados(mockAtletaId)).thenReturn(List.of(prof));

            mockMvc.perform(get("/api/v1/atleta/profesionales").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].nombre").value("Kevin Garcia"));
        }

        @Test
        @DisplayName("Debe permitir darse de baja de un entrenador")
        void desconectarAtleta_Exito() throws Exception {
            UUID profesionalId = UUID.randomUUID();
            doNothing().when(asignacionService).terminarAsignacion(profesionalId, mockAtletaId);

            mockMvc.perform(delete("/api/v1/atleta/profesionales/{profesionalId}", profesionalId)
                            .with(user(principal)).with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Debe establecer conexión mediante código de invitación")
        void conectarConEntrenador_Exito() throws Exception {
            AsignacionRequestDTO requestDto = new AsignacionRequestDTO("GRIT-23E1-234N", TipoServicio.ENTRENAMIENTO);
            doNothing().when(asignacionService).conectarConEntrenador(eq(mockAtletaId), any(AsignacionRequestDTO.class));

            mockMvc.perform(post("/api/v1/atleta/conectar")
                            .with(user(principal)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.ok").value(true));
        }
    }

    // ==========================================
    // SECCIÓN: CONTROL DE PESO
    // ==========================================
    @Nested
    @DisplayName("Endpoints de Seguimiento de Peso")
    class PesoTests {

        @Test
        @DisplayName("Debe obtener solicitudes de pesaje pendientes")
        void getPendiente_Exito() throws Exception {
            SolicitudPendienteDTO solicitud = new SolicitudPendienteDTO(UUID.randomUUID(), LocalDate.now(),"Falta pesaje semanal");
            when(pesoService.obtenerSolicitudPendiente(mockAtletaId)).thenReturn(solicitud);

            mockMvc.perform(get("/api/v1/atleta/peso/solicitud-pendiente").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));
        }

        @Test
        @DisplayName("Debe registrar un nuevo pesaje con éxito")
        void registrarPeso_Exito() throws Exception {
            PesoRequestDTO request = new PesoRequestDTO(UUID.randomUUID(), BigDecimal.valueOf(82.5));
            PesoResponseDTO response = new PesoResponseDTO(UUID.randomUUID(), LocalDate.now(), BigDecimal.valueOf(82.5));

            when(pesoService.registrarPeso(any(PesoRequestDTO.class), eq(mockAtletaId))).thenReturn(response);

            mockMvc.perform(post("/api/v1/atleta/peso")
                            .with(user(principal)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.pesoKg").value(82.5));
        }

        @Test
        @DisplayName("Debe recuperar el histórico completo de peso")
        void getHistorial_Exito() throws Exception {
            HistorialPesoDTO h1 = new HistorialPesoDTO(UUID.randomUUID(), LocalDate.now().minusWeeks(1), BigDecimal.valueOf(85.0), "pepe");
            HistorialPesoDTO h2 = new HistorialPesoDTO(UUID.randomUUID(), LocalDate.now().minusWeeks(1), BigDecimal.valueOf(85.0), "pepe");

            when(pesoService.obtenerHistorialAtleta(mockAtletaId)).thenReturn(List.of(h1, h2));

            mockMvc.perform(get("/api/v1/atleta/peso/historial").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    // ==========================================
    // SECCIÓN: CONTROL DE CUENTA Y CREDENCIALES
    // ==========================================
    @Nested
    @DisplayName("Endpoints de Seguridad de la Cuenta")
    class CuentaTests {

        @Test
        @DisplayName("Debe actualizar el password correctamente")
        void updatePassword_Exito() throws Exception {
            PasswordUpdateDTO passDto = new PasswordUpdateDTO("Antigua123!", "Nueva2026_Secure!");
            doNothing().when(usuarioService).actualizarPassword(eq(mockAtletaId), any(PasswordUpdateDTO.class));

            mockMvc.perform(put("/api/v1/atleta/password")
                            .with(user(principal)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passDto)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Debe procesar la baja de cuenta y limpiar las cookies de autenticación")
        void eliminarCuenta_Exito() throws Exception {
            doNothing().when(atletaService).solicitarBajaCuenta(mockAtletaId);

            // Forzamos los mocks de borrado de cookies para prevenir NullPointerException en cabeceras
            when(jwtUtils.getCleanAccessCookie()).thenReturn(ResponseCookie.from("access_token", "").maxAge(0).path("/").build());
            when(jwtUtils.getCleanRefreshCookie()).thenReturn(ResponseCookie.from("refresh_token", "").maxAge(0).path("/").build());

            mockMvc.perform(delete("/api/v1/atleta/cuenta")
                            .with(user(principal)).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE));

            verify(atletaService, times(1)).solicitarBajaCuenta(mockAtletaId);
        }
    }
}
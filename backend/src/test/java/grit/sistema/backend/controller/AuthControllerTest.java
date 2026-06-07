package grit.sistema.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.auth.LoginRequestDTO;
import grit.sistema.backend.dto.auth.LoginResponseDTO;
import grit.sistema.backend.dto.coaching.*;
import grit.sistema.backend.dto.user.MeResponseDTO;
import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.entity.coaching.enums.EstadoRevision;
import grit.sistema.backend.entity.coaching.enums.TitulacionEntrenamiento;
import grit.sistema.backend.entity.coaching.enums.TitulacionNutricion;
import grit.sistema.backend.entity.common.enums.EstadoUsuario;
import grit.sistema.backend.entity.common.enums.Rol;
import grit.sistema.backend.security.jwt.JwtUtils;
import grit.sistema.backend.security.jwt.TokenStoreService;
import grit.sistema.backend.security.model.UserPrincipal;
import grit.sistema.backend.service.auth.AuthService;
import grit.sistema.backend.service.coaching.AtletaService;
import grit.sistema.backend.service.coaching.EntrenadorService;
import grit.sistema.backend.service.user.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

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
@DisplayName("Pruebas Unitarias/Integración - Controlador de Autenticación")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;
    @MockBean
    private AuthService authService;
    @MockBean
    private AtletaService atletaService;
    @MockBean
    private EntrenadorService entrenadorService;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private TokenStoreService tokenStoreService;

    // ==========================================
    // SECCIÓN: REGISTRO DE ENTRENADOR
    // ==========================================
    @Nested
    @DisplayName("Resgistro Entrenador")
    class RegistroEntrenadorTests {

        @Test
        @DisplayName("Debe registrar entrenador exitosamente cuando cumple con las validaciones estrictas y adjunta documentos")
        void registrarEntrenador_Exito() throws Exception {
            EntrenadorRequestDTO requestDto = new EntrenadorRequestDTO();
            requestDto.setNombre("Kevin Garcia");
            requestDto.setEmail("kevin@gmail.com");
            requestDto.setPassword("Grit2026_Secure!");
            requestDto.setCodigoProfesional("MU-12345");
            requestDto.setTitulacionEntrenamiento(TitulacionEntrenamiento.GRADO_CAFYD);
            requestDto.setTitulacionNutricion(TitulacionNutricion.GRADO_NUTRICION_DIETETICA);
            requestDto.setExperienciaAnos((short) 5);
            requestDto.setDescripcion("Especialista en entrenamiento de fuerza y rehabilitación.");
            requestDto.setMasters(List.of("Máster en Alto Rendimiento"));

            UUID mockId = UUID.randomUUID();
            EntrenadorResponseDTO responseDto = new EntrenadorResponseDTO(
                    mockId,
                    "Kevin Garcia",
                    Rol.ENTRENADOR,
                    EstadoRevision.PENDIENTE_REVISION,
                    true,
                    true
            );

            MockMultipartFile datosPart = new MockMultipartFile(
                    "datos",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(requestDto)
            );

            MockMultipartFile certificacionPart = new MockMultipartFile(
                    "certificaciones",
                    "certificados.pdf",
                    MediaType.APPLICATION_PDF_VALUE,
                    "PDF_MOCK_CONTENT".getBytes()
            );

            when(entrenadorService.registrarEntrenador(any(EntrenadorRequestDTO.class), any(), any())).thenReturn(responseDto);

            mockMvc.perform(multipart("/api/v1/auth/registro/entrenador")
                            .file(datosPart)
                            .file(certificacionPart)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.ok").value(true))
                    .andExpect(jsonPath("$.data.id").value(mockId.toString()))
                    .andExpect(jsonPath("$.data.rol").value("ENTRENADOR"))
                    .andExpect(jsonPath("$.data.estado").value("PENDIENTE_REVISION"))
                    .andExpect(jsonPath("$.data.tituloEntrenamiento").value(true));
        }

        @Test
        @DisplayName("Debe fallar con 400 Bad Request cuando la contraseña no es lo suficientemente compleja")
        void registrarEntrenador_PasswordDebil_Retorna400() throws Exception {
            EntrenadorRequestDTO requestDtoInvalido = new EntrenadorRequestDTO();
            requestDtoInvalido.setNombre("Kevin Garcia");
            requestDtoInvalido.setEmail("kevin@gmail.com");
            requestDtoInvalido.setPassword("12345");

            MockMultipartFile datosPart = new MockMultipartFile(
                    "datos", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(requestDtoInvalido)
            );
            MockMultipartFile certificacionPart = new MockMultipartFile(
                    "certificaciones", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "test".getBytes()
            );

            mockMvc.perform(multipart("/api/v1/auth/registro/entrenador")
                            .file(datosPart)
                            .file(certificacionPart)
                            .with(csrf()))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(entrenadorService);
        }

    }

    @Nested
    @DisplayName("Registro de Atletas")
    class RegistroAtletaTests {

        @Test
        @DisplayName("Debe registrar atleta exitosamente, iniciar sesión y retornar las cookies de autenticación")
        void registrarAtleta_Exito() throws Exception {
            String atletaRequestJson = """
                    {
                        "nombre": "Atleta Juan",
                        "email": "atleta@test.com",
                        "password": "Grit2026_Secure!",
                        "fechaNac": "2000-12-22",
                        "deporte": "CICLISMO",
                        "servicio": "ENTRENAMIENTO",
                        "genero": "HOMBRE",
                        "nivel": "PRINCIPIANTE",
                        "pesoKg": 80.0,
                        "alturaCm": 180.0
                    }
                    """;

            MockMultipartFile datosPart = new MockMultipartFile(
                    "datos",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    atletaRequestJson.getBytes()
            );

            MockMultipartFile fotoPart = new MockMultipartFile(
                    "fotoPerfil",
                    "avatar.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "FOTO_BINARY_DATA".getBytes()
            );

            UUID mockId = UUID.randomUUID();
            AtletaResponseDTO responseDto = new AtletaResponseDTO(true, "Atleta creado", new AtletaData(mockId, "ACTIVO", "ATLETA"));

            when(atletaService.registrarAtleta(any(AtletaRequestDTO.class), any())).thenReturn(responseDto);

            ResponseCookie mockAccessCookie = ResponseCookie.from("access_token", "jwt-atleta-access").path("/").build();
            ResponseCookie mockRefreshCookie = ResponseCookie.from("refresh_token", "jwt-atleta-refresh").path("/").build();

            when(jwtUtils.generateAccessCookie(eq("atleta@test.com"), eq(Rol.ATLETA.name()), anyString())).thenReturn(mockAccessCookie);
            when(jwtUtils.generateRefreshCookie(eq("atleta@test.com"), anyString())).thenReturn(mockRefreshCookie);


            mockMvc.perform(multipart("/api/v1/auth/registro/atleta")
                            .file(datosPart)
                            .file(fotoPart)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.ok").value(true))
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.data.id").value(mockId.toString()))
                    .andExpect(jsonPath("$.data.estado").value("ACTIVO"))
                    .andExpect(jsonPath("$.data.rol").value("ATLETA"));
        }

        @Test
        @DisplayName("Debe registrar atleta exitosamente incluso si la foto de perfil no se envía (campo opcional)")
        void registrarAtleta_SinFoto_Exito() throws Exception {
            String atletaRequestJson = """
                    {
                        "nombre": "Atleta Juan",
                        "email": "atleta@test.com",
                        "password": "Grit2026_Secure!",
                        "fechaNac": "2000-12-22",
                        "deporte": "CICLISMO",
                        "servicio": "ENTRENAMIENTO",
                        "genero": "HOMBRE",
                        "nivel": "PRINCIPIANTE",
                        "pesoKg": 80.0,
                        "alturaCm": 180.0
                    }
                    """;

            MockMultipartFile datosPart = new MockMultipartFile(
                    "datos", "", MediaType.APPLICATION_JSON_VALUE, atletaRequestJson.getBytes()
            );

            UUID mockId = UUID.randomUUID();
            AtletaResponseDTO responseDto = new AtletaResponseDTO(true, "Atleta creado", new AtletaData(mockId, "ACTIVO", "ATLETA"));

            when(atletaService.registrarAtleta(any(AtletaRequestDTO.class), any())).thenReturn(responseDto);

            ResponseCookie mockAccessCookie = ResponseCookie.from("access_token", "jwt-atleta-access").path("/").build();
            ResponseCookie mockRefreshCookie = ResponseCookie.from("refresh_token", "jwt-atleta-refresh").path("/").build();

            when(jwtUtils.generateAccessCookie(eq("atleta@test.com"), eq(Rol.ATLETA.name()), anyString())).thenReturn(mockAccessCookie);
            when(jwtUtils.generateRefreshCookie(eq("atleta@test.com"), anyString())).thenReturn(mockRefreshCookie);

            // Act & Assert
            mockMvc.perform(multipart("/api/v1/auth/registro/atleta")
                            .file(datosPart)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.ok").value(true))
                    .andExpect(jsonPath("$.data.id").value(mockId.toString()))
                    .andExpect(jsonPath("$.data.estado").value("ACTIVO"))
                    .andExpect(jsonPath("$.data.rol").value("ATLETA"));
        }
    }

    // ==========================================
    // SECCIÓN: LOGIN Y MANEJO DE COOKIES
    // ==========================================
    @Nested
    @DisplayName("Login y logout")
    class LoginTests {

        @Test
        @DisplayName("Debe responder HttpStatus.CREATED con la estructura de LoginResponseDTO y Cookies adecuadas")
        void login_Exito() throws Exception {
            LoginRequestDTO loginRequest = new LoginRequestDTO("kevin@gmail.com", "Grit2026_Secure!");

            LoginData loginData = new LoginData(
                    Rol.ENTRENADOR.name(),
                    EstadoRevision.PENDIENTE_REVISION.name(),
                    "Kevin Garcia",
                    true,
                    true,
                    "PREMIUM"
            );
            LoginResponseDTO loginResponse = new LoginResponseDTO(true, loginData);

            when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

            ResponseCookie mockAccessCookie = ResponseCookie.from("access_token", "jwt-access-token").path("/").build();
            ResponseCookie mockRefreshCookie = ResponseCookie.from("refresh_token", "jwt-refresh-token").path("/").build();

            when(jwtUtils.generateAccessCookie(eq("kevin@gmail.com"), eq(Rol.ENTRENADOR.name()), anyString())).thenReturn(mockAccessCookie);
            when(jwtUtils.generateRefreshCookie(eq("kevin@gmail.com"), anyString())).thenReturn(mockRefreshCookie);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.ok").value(true))
                    .andExpect(jsonPath("$.data.nombre").value("Kevin Garcia"))
                    .andExpect(jsonPath("$.data.rol").value("ENTRENADOR"));
        }

        @Test
        @DisplayName("Debe procesar Logout limpiando las cookies de sesión")
        void logout_Exito() throws Exception {
            when(jwtUtils.getCleanAccessCookie()).thenReturn(ResponseCookie.from("access_token", "").maxAge(0).path("/").build());
            when(jwtUtils.getCleanRefreshCookie()).thenReturn(ResponseCookie.from("refresh_token", "").maxAge(0).path("/").build());

            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE));
        }
    }

    // ==========================================
    // SECCIÓN: ROTACIÓN Y SEGURIDAD DEL REFRESH
    // ==========================================
    @Nested
    @DisplayName("Mecanismo de Refresco de Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Debe refrescar sesión si el refresh token es válido y no ha sido reutilizado")
        void refresh_Exito() throws Exception {
            String tokenSimulado = "refresh-token-valido";
            MockCookie cookieRefresh = new MockCookie("refresh_token", tokenSimulado);

            when(jwtUtils.esTokenValido(eq(tokenSimulado), any())).thenReturn(true);
            when(jwtUtils.extraerEmail(tokenSimulado)).thenReturn("user@test.com");
            when(jwtUtils.extraerJti(tokenSimulado)).thenReturn("uuid-jti-1234");

            when(tokenStoreService.isReuseDetected("uuid-jti-1234")).thenReturn(false);

            LoginData loginData = new LoginData("ATLETA", "ACTIVO", "user@test.com", true, true, "ENTRENAMIENTO");
            when(usuarioService.obtenerDatosParaRefresh("user@test.com")).thenReturn(loginData);

            when(jwtUtils.generateAccessCookie(any(), any(), any())).thenReturn(ResponseCookie.from("access_token", "new-access").build());
            when(jwtUtils.generateRefreshCookie(any(), any())).thenReturn(ResponseCookie.from("refresh_token", "new-refresh").build());

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(cookieRefresh)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE));

            verify(tokenStoreService, times(1)).burnJti("uuid-jti-1234");
        }

        @Test
        @DisplayName("Debe retornar 403 Forbidden si se detecta reuso del Refresh Token (Ataque de suplantación)")
        void refresh_ReusoDetectado_Retorna403() throws Exception {
            String tokenRobado = "refresh-token-robado";
            MockCookie cookieRefresh = new MockCookie("refresh_token", tokenRobado);

            when(jwtUtils.esTokenValido(eq(tokenRobado), any())).thenReturn(true);
            when(jwtUtils.extraerEmail(tokenRobado)).thenReturn("victima@test.com");
            when(jwtUtils.extraerJti(tokenRobado)).thenReturn("jti-atacado");

            when(tokenStoreService.isReuseDetected("jti-atacado")).thenReturn(true);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(cookieRefresh)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(usuarioService);
        }
    }

    @Nested
    @DisplayName("Endpoint Perfil Actual (/me)")
    class UserProfileTests {

        @Test
        @DisplayName("Debe retornar los datos del perfil si la sesión de Spring Security está activa")
        void getCurrentUser_Autenticado_Exito() throws Exception {
            UUID mockUserId = UUID.randomUUID();

            Usuario test = new Usuario();
            test.setId(mockUserId);
            test.setNombre("Kevin Garcia");
            test.setEmail("kevin@gmail.com");
            test.setRol(Rol.ADMIN);
            test.setEstado(EstadoUsuario.ACTIVO);
            test.setPassword("password123");

            UserPrincipal principal = new UserPrincipal(test);

            MeResponseDTO mockProfile = new MeResponseDTO(
                    true,
                    new MeResponseDTO.MeData(
                            mockUserId,
                            "kevin@gmail.com",
                            "ADMIN",
                            "ACTIVO",
                            "Kevin Garcia",
                            null, null, null, null, null
                    )
            );

            when(usuarioService.obtenerMiInformacion(eq(mockUserId))).thenReturn(mockProfile);

            mockMvc.perform(get("/api/v1/auth/me")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(mockUserId.toString()))
                    .andExpect(jsonPath("$.data.rol").value("ADMIN"))
                    .andExpect(jsonPath("$.data.nombre").value("kevin@gmail.com"))
                    .andExpect(jsonPath("$.data.estado").value("ACTIVO"));
        }

        @Test
        @DisplayName("Debe denegar el acceso con 401 Unauthorized si no hay sesión activa")
        void getCurrentUser_SinAutenticar_Retorna401() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
package grit.sistema.backend;

import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.model.enums.TitulacionEntrenamiento;
import grit.sistema.backend.model.enums.TitulacionNutricion;
import grit.sistema.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Debe registrar un Atleta exitosamente")
    void debeRegistrarAtleta() throws Exception {
        String registroJson = """
                            {
                              "nombre": "Kevin test",
                              "email": "test@gmail.com",
                              "password": "********",
                              "fechaNac": "2000-04-09",
                              "genero": "hombre",
                              "pesoKg": 30,
                              "alturaCm": 100,
                              "deporte": "futbol",
                              "nivel": "PRINCIPIANTE",
                              "servicio": "ENTRENAMIENTO",
                              "objetivo": "RENDIMIENTO"
                            }
                """;

        mockMvc.perform(post("/api/v1/auth/registro/atleta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.rol").value("ATLETA"))
                .andExpect(jsonPath("$.data.estado").value("ACTIVO"))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().httpOnly("access_token", true));

        // Asegúrate de inyectar AtletaRepository o usar el de Usuario
        var usuarioGuardado = usuarioRepository.findByEmail("test@gmail.com");
        assertTrue(usuarioGuardado.isPresent(), "El usuario debería existir en la base de datos");
        assertEquals(Rol.ATLETA, usuarioGuardado.get().getRol());
    }

    @Test
    @DisplayName("Debe registrar un Entrenador exitosamente con documentos")
    void debeRegistrarEntrenador() throws Exception {
        // 1. Preparamos un archivo simulado (PDF de prueba)
        MockMultipartFile archivoPdf = new MockMultipartFile(
                "documentos",
                "titulo.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "contenido del titulo".getBytes()
        );

        // 2. Realizamos la petición multipart
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/auth/registro/entrenador")
                        .file(archivoPdf)
                        .param("nombre", "Coach Kevin")
                        .param("email", "coach@test.com")
                        .param("password", "password123")
                        .param("titulacionEntrenamiento", TitulacionEntrenamiento.GRADO_CAFYD.name())
                        .param("titulacionNutricion", TitulacionNutricion.GRADO_NUTRICION_DIETETICA.name())
                        .param("codigoProfesional", "COL-12345")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print()) // Útil para debuguear si algo falla
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value(containsString("Solicitud recibida")))
                .andExpect(jsonPath("$.data.rol").value("ENTRENADOR"));

        // 3. Verificación opcional en DB
        assertTrue(usuarioRepository.findByEmail("coach@test.com").isPresent());
    }

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void debeLoguearExitosamente() throws Exception {

        // 2. Act: Intentar login con las mismas credenciales
        String loginJson = """
            {
                "email": "test@gmail.com",
                "password": "********"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    @DisplayName("Login fallido con contraseña incorrecta")
    void debeFallarLoginConPasswordErroneo() throws Exception {

        // 2. Act: Login con password mal escrito
        String loginErroneoJson = """
            {
                "email": "test@gmail.com",
                "password": "password_incorrecto"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginErroneoJson))
                .andExpect(status().isUnauthorized()); // Debería devolver 401
    }

    @Test
    @DisplayName("Debe fallar con 400 si no se envían titulaciones")
    void debeFallarSinTitulaciones() throws Exception {
        MockMultipartFile archivoPdf = new MockMultipartFile(
                "documentos",
                "titulo.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "contenido del titulo".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/auth/registro/entrenador")
                        .file(archivoPdf)
                        .param("nombre", "Coach Kevin")
                        .param("email", "coach@test.com")
                        .param("password", "password123")

                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest()) // Esperamos 400, no 500
                .andExpect(jsonPath("$.codigo").value(400));
    }


}

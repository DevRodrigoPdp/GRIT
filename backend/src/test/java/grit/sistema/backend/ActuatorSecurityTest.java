package grit.sistema.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Configura el cliente HTTP de prueba
@ActiveProfiles("test") // Usa el profile de test (H2 en memoria)
public class ActuatorSecurityTest {

    @Autowired // La inyección DEBE ser así en los tests de Spring
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debe permitir acceso público al endpoint de salud (Health)")
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/management/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Debe denegar el acceso a métricas si el usuario no está autenticado")
    void metricsIsProtected() throws Exception {
        // Al no enviar cookie ni token, debe saltar el 401
        mockMvc.perform(get("/management/metrics"))
                .andExpect(status().isUnauthorized());
    }

}

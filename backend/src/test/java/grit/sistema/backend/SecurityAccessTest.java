package grit.sistema.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc // Configura el cliente HTTP de prueba
@ActiveProfiles("test") // Usa el profile de test (H2 en memoria)
public class SecurityAccessTest {

    @Autowired // La inyección DEBE ser así en los tests de Spring
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debe de denegar el acceso a admin si el usuario no está autenticado")
    void shouldDenyAccessToAdminUnAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ATLETA")
    @DisplayName("Debe denegar el acceso a /admin si el usuario es ATLETA")
    void shouldDenyAccessToAdminForAtleta() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Debe permitir el acceso a /admin si el usuario es ADMIN")
    void shouldAllowAccessToAdminForAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isOk());
    }
}

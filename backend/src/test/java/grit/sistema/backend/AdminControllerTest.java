package grit.sistema.backend;

import grit.sistema.backend.model.Usuario;
import grit.sistema.backend.model.enums.EstadoUsuario;
import grit.sistema.backend.model.enums.Rol;
import grit.sistema.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // IMPORTANTE: Esto limpia la base de datos después de cada test automáticamente
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin puede ver lista de usuarios tras insertar uno")
    void adminCanSeeUserList() throws Exception {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan Perez");
        usuario.setEmail("juan@example.com");
        usuario.setPassword("12345678");
        usuario.setRol(Rol.ATLETA); // O el enum que corresponda
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(usuario);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) // Verificamos que es una lista
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"))
                .andExpect(jsonPath("$[0].email").value("juan@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Debe devolver 400 cuando el formato del email es inválido")
    void shouldReturn400ForInvalidEmail() throws Exception {
        String usuarioInvalidoJson = """
        {
            "nombre": "Juan",
            "email": "email-sin-formato",
            "password": "123"
        }
        """;

        mockMvc.perform(post("/api/v1/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioInvalidoJson))
                .andExpect(status().isBadRequest()) // Verifica que devuelve 400
                .andExpect(jsonPath("$.mensaje").value(containsString("Error de validación")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Debe devolver 409 cuando el email ya existe")
    void shouldReturn409WhenEmailExists() throws Exception {
        // 1. Arrange: Insertar el primer usuario
        Usuario existente = new Usuario();
        existente.setNombre("Juan");
        existente.setEmail("repetido@test.com");
        existente.setPassword("hash");
        existente.setRol(Rol.ATLETA);
        existente.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.saveAndFlush(existente);

        // 2. Act & Assert: Intentar insertar el mismo email a través del endpoint
        String usuarioJson = """
        {
            "nombre": "Otro Juan",
            "email": "repetido@test.com",
            "password": "password123",
            "rol": "ATLETA"
        }
        """;

        mockMvc.perform(post("/api/v1/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson))
                .andDo(print()) // MIRA EL CUERPO DE LA RESPUESTA AQUÍ
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }
}

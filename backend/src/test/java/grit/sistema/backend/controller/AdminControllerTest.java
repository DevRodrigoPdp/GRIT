package grit.sistema.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import grit.sistema.backend.dto.EntrenadorBusquedaTestDTO;
import grit.sistema.backend.dto.UsuarioBusquedaTestDTO;
import grit.sistema.backend.dto.coaching.EntrenadorBusquedaDTO;
import grit.sistema.backend.dto.coaching.EntrenadorPendienteDTO;
import grit.sistema.backend.dto.user.UsuarioBusquedaDTO;
import grit.sistema.backend.dto.user.UsuarioResponseDTO;
import grit.sistema.backend.service.admin.AdminService;
import grit.sistema.backend.service.user.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas Controlador de Administración (AdminController)")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private UsuarioService usuarioService;

    // ==========================================
    // SECCIÓN: SEGURIDAD Y ACCESOS ROL-BASED
    // ==========================================
    @Nested
    @DisplayName("Validación de Restricciones de Seguridad")
    class SecurityTests {

        @Test
        @DisplayName("Debe rechazar con 401 Unauthorized si la petición no está autenticada")
        void sinAutenticacion_Retorna401() throws Exception {
            mockMvc.perform(get("/api/v1/admin/usuarios"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ATLETA") // Simulamos un usuario logueado con rol incorrecto
        @DisplayName("Debe rechazar con 403 Forbidden si el usuario no tiene rol ADMIN")
        void rolIncorrecto_Retorna403() throws Exception {
            mockMvc.perform(get("/api/v1/admin/usuarios"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // SECCIÓN: GESTIÓN Y BÚSQUEDA DE USUARIOS
    // ==========================================
    @Nested
    @DisplayName("Endpoints de Gestión de Usuarios")
    @WithMockUser(roles = "ADMIN") // Aplica el rol de administrador a todos los tests de este bloque
    class UsuarioManagementTests {

        @Test
        @DisplayName("Debe listar todos los usuarios sin paginar")
        void getAllUsuarios_Exito() throws Exception {
            UsuarioResponseDTO u1 = new UsuarioResponseDTO(UUID.randomUUID().toString(), "Admin", "admin@test.com", "ADMIN", "ACTIVO", OffsetDateTime.now());
            UsuarioResponseDTO u2 = new UsuarioResponseDTO(UUID.randomUUID().toString(), "Atleta", "atleta@test.com", "ATLETA", "ACTIVO", OffsetDateTime.now());

            when(usuarioService.findAll()).thenReturn(List.of(u1, u2));

            mockMvc.perform(get("/api/v1/admin/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nombre").value("Admin"))
                    .andExpect(jsonPath("$[1].email").value("atleta@test.com"));
        }

        @Test
        @DisplayName("Debe buscar usuarios de manera flexible y paginada")
        void buscarUsuarios_Paginado_Exito() throws Exception {
            UsuarioBusquedaDTO dto = new UsuarioBusquedaTestDTO(UUID.randomUUID(), "Juan", "juan@test.com", "ATLETA", "ACTIVO", Instant.now());
            Page<UsuarioBusquedaDTO> pagedResponse = new PageImpl<>(List.of(dto), PageRequest.of(0, 15), 1);

            when(adminService.buscarUsuarios(eq("Juan"), any(Pageable.class))).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/usuarios/search")
                            .param("search", "Juan")
                            .param("page", "0")
                            .param("size", "15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].nombre").value("Juan"))
                    .andExpect(jsonPath("$.page.totalElements").value(1));
        }

        @Test
        @DisplayName("Debe alternar el estado del usuario (Bloquear/Desbloquear) con método PATCH")
        void alternarEstadoUsuario_Exito() throws Exception {
            UUID mockId = UUID.randomUUID();
            UsuarioResponseDTO response = new UsuarioResponseDTO(mockId.toString(), "Kevin", "kevin@test.com", "ENTRENADOR", "BLOQUEADO", OffsetDateTime.now());

            when(adminService.alternarEstadoUsuario(eq(mockId))).thenReturn(response);

            mockMvc.perform(patch("/api/v1/admin/usuarios/{id}", mockId)
                            .with(csrf())) // 👈 Requerido para operaciones mutables (POST, PATCH, DELETE)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("BLOQUEADO"))
                    .andExpect(jsonPath("$.idPublico").value(mockId.toString()));
        }

        @Test
        @DisplayName("Debe eliminar definitivamente un usuario y sus recursos")
        void eliminarUsuario_Exito() throws Exception {
            UUID mockId = UUID.randomUUID();

            doNothing().when(adminService).eliminarUsuarioCompleto(mockId);

            mockMvc.perform(delete("/api/v1/admin/usuarios/{id}", mockId)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(adminService, times(1)).eliminarUsuarioCompleto(mockId);
        }
    }

    // ==========================================
    // SECCIÓN: REVISIONES DE ENTRENADORES
    // ==========================================
    @Nested
    @DisplayName("Endpoints de Revisión de Entrenadores")
    @WithMockUser(roles = "ADMIN")
    class EntrenadorRevisionTests {

        @Test
        @DisplayName("Debe listar entrenadores pendientes de revisión")
        void getPendientes_Exito() throws Exception {
            EntrenadorPendienteDTO pendiente = new EntrenadorPendienteDTO(UUID.randomUUID(), "Carlos Entrenador", "carlos@test.com", "MU-999","MU-999","", OffsetDateTime.now(),List.of());
            Page<EntrenadorPendienteDTO> pagedResponse = new PageImpl<>(List.of(pendiente), PageRequest.of(0, 15), 1);

            when(adminService.obtenerPendientes(0, 15)).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/entrenadores/pendientes")
                            .param("page", "0")
                            .param("size", "15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].nombre").value("Carlos Entrenador"));
        }

        @Test
        @DisplayName("Debe buscar en los entrenadores pendientes a través del buscador")
        void getPendientesBuscador_Exito() throws Exception {
            EntrenadorBusquedaDTO dto = new EntrenadorBusquedaTestDTO(UUID.randomUUID(), "Carlos", "carlos@test.com", "PENDIENTE_REVISION","lskfjlaks", OffsetDateTime.now());
            Page<EntrenadorBusquedaDTO> pagedResponse = new PageImpl<>(List.of(dto), PageRequest.of(0, 15), 1);

            when(adminService.obtenerPendientesBuscador(eq("Carlos"), eq(0), eq(15))).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/admin/entrenadores/pendientes/search")
                            .param("search", "Carlos")
                            .param("page", "0")
                            .param("size", "15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].nombre").value("Carlos"));
        }

        @Test
        @DisplayName("Debe procesar la aprobación o rechazo de un entrenador de forma exitosa")
        void procesarRevision_Exito() throws Exception {
            UUID mockId = UUID.randomUUID();

            doNothing().when(adminService).procesarAprobacion(eq(mockId), eq(true), eq("Documentación correcta"));

            mockMvc.perform(post("/api/v1/admin/entrenadores/{id}/revision", mockId)
                            .param("aprobado", "true")
                            .param("motivo", "Documentación correcta")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(adminService, times(1)).procesarAprobacion(mockId, true, "Documentación correcta");
        }
    }
}
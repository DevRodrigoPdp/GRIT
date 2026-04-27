package grit.sistema.backend;

import grit.sistema.backend.entity.Usuario;
import grit.sistema.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioRepositoryIT extends AbstractIntegrationTest
{
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Debe persistir un usuario correctamente en el contenedor de PostgreSQL")
    void debeGuardarYBuscarUsuario() {
        // Given: Preparamos los datos
        Usuario usuario = new Usuario();
        String nombreEsperado = "Junior Prometedor";
        usuario.setNombre(nombreEsperado);
        usuario.setEmail("junior@empresa.com");
        usuario.setPassword("hash_seguro_123");

        // When: Ejecutamos la acción
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Then: Verificamos resultados
        Optional<Usuario> encontrada = usuarioRepository.findById(usuarioGuardado.getId());

        assertTrue(encontrada.isPresent(), "El usuario debería existir en la DB");
        assertEquals(nombreEsperado, encontrada.get().getNombre(), "El nombre guardado no coincide");
    }
}

package grit.sistema.backend.service.admin;

import grit.sistema.backend.dto.coaching.EntrenadorPendienteDTO;
import grit.sistema.backend.dto.user.UsuarioBusquedaDTO;
import grit.sistema.backend.dto.user.UsuarioResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {

    Page<UsuarioBusquedaDTO> buscarUsuarios(String termino, Pageable pageable);

    Page<EntrenadorPendienteDTO> obtenerPendientes(int page, int size);

    Page<EntrenadorPendienteDTO> obtenerPendientesBuscador(String searchTerm, int page, int size);

    void procesarAprobacion(UUID id, boolean aprobado, String motivo);

    void eliminarUsuarioCompleto(UUID id);

    UsuarioResponseDTO alternarEstadoUsuario(UUID id);
}

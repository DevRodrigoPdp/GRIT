package grit.sistema.backend.service.user;

import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.user.MeResponseDTO;
import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.user.UsuarioDTO;
import grit.sistema.backend.dto.user.UsuarioResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UsuarioService {

    List<UsuarioResponseDTO> findAll();

    LoginData obtenerDatosParaRefresh(String email);

    UsuarioDTO findByEmail(String email);

    MeResponseDTO obtenerMiInformacion(String email);

    UsuarioDTO obtenerUsuarioActual();

    void actualizarPassword(UUID usuarioId, PasswordUpdateDTO dto);

    void suspenderUsuario(UUID usuarioId);
}

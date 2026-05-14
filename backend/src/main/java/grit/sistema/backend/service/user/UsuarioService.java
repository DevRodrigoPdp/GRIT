package grit.sistema.backend.service.user;

import grit.sistema.backend.dto.auth.PasswordUpdateDTO;
import grit.sistema.backend.dto.coaching.FotoPerfilResponseDTO;
import grit.sistema.backend.dto.user.MeResponseDTO;
import grit.sistema.backend.dto.auth.LoginData;
import grit.sistema.backend.dto.user.UsuarioDTO;
import grit.sistema.backend.dto.user.UsuarioResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> findAll();

    LoginData obtenerDatosParaRefresh(String email);

    MeResponseDTO obtenerMiInformacion(String email);

    UsuarioDTO obtenerUsuarioActual();

    FotoPerfilResponseDTO actualizarFotoPerfil(String email,  MultipartFile foto);

    void actualizarPassword(String email, PasswordUpdateDTO dto);

    void suspenderUsuario(String email);
}

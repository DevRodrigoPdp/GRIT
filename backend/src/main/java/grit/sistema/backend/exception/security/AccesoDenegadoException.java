package grit.sistema.backend.exception.security;

import org.springframework.security.access.AccessDeniedException;

public class AccesoDenegadoException extends AccessDeniedException {
    public AccesoDenegadoException(String mensaje){
        super(mensaje);
    }
}

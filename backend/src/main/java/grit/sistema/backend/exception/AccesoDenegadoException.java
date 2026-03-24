package grit.sistema.backend.exception;

public class AccesoDenegadoException extends org.springframework.security.access.AccessDeniedException{
    public AccesoDenegadoException(String mensaje){
        super(mensaje);
    }
}

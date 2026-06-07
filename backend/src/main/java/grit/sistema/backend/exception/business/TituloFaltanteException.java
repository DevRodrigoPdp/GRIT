package grit.sistema.backend.exception.business;

import grit.sistema.backend.exception.security.AccesoDenegadoException;

public class TituloFaltanteException extends AccesoDenegadoException {
    public TituloFaltanteException(String message) {
        super(message);
    }
}

package grit.sistema.backend.exception.security;

public class PwnedPasswordException extends RuntimeException {
    public PwnedPasswordException(String message) {
        super(message);
    }
}

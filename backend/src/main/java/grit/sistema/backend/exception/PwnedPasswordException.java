package grit.sistema.backend.exception;

public class PwnedPasswordException extends RuntimeException {
    public PwnedPasswordException(String message) {
        super(message);
    }
}

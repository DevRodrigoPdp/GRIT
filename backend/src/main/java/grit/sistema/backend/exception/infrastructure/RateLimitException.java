package grit.sistema.backend.exception.infrastructure;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}

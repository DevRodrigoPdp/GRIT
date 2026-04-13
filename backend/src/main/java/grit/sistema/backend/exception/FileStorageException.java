package grit.sistema.backend.exception;

/**
 * Excepción personalizada para errores en la gestión de archivos.
 * Heredamos de RuntimeException para que Spring pueda capturarla
 * sin obligar a usar bloques try-catch en toda la lógica.
 */
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

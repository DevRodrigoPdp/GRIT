package grit.sistema.backend.exception;

import grit.sistema.backend.dto.error.ErrorRespuestaDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Errores de Seguridad (401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse("Credenciales inválidas. Verifique su usuario o contraseña.", HttpStatus.UNAUTHORIZED, request);
    }

    // 2. Errores de Seguridad (403) - CORREGIDO EL IMPORT INTERNO
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleAccessDenied(Exception ex, HttpServletRequest request) {
        return buildErrorResponse("No tienes permisos para acceder a este recurso.", HttpStatus.FORBIDDEN, request);
    }

    // 3. Errores de Validación (400) - REFACTORIZADO
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorRespuestaDTO> handleValidationErrors(MethodArgumentNotValidException e, HttpServletRequest request) {
        // Unificamos los errores en un solo string o podrías mejorar el DTO para aceptar una lista
        String mensaje = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse("Error de validación: " + mensaje, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                "Datos de entrada inválidos: " + ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 4. Errores de Recursos No Encontrados (404)
    @ExceptionHandler({EntityNotFoundException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorRespuestaDTO> handleNotFound(Exception ex, HttpServletRequest request) {
        String mensaje = (ex instanceof MethodArgumentTypeMismatchException)
                ? "Formato de parámetro inválido"
                : ex.getMessage();
        return buildErrorResponse(mensaje, HttpStatus.NOT_FOUND, request);
    }

    // 5. Conflictos de Negocio (409)
    @ExceptionHandler({DataIntegrityViolationException.class, UsuarioExistenteException.class, SesionActivaException.class})
    public ResponseEntity<ErrorRespuestaDTO> handleConflicts(Exception ex, HttpServletRequest request) {
        String mensaje = "Conflicto en la operación: El registro ya existe o viola restricciones.";
        if (ex instanceof UsuarioExistenteException || ex instanceof SesionActivaException) {
            mensaje = ex.getMessage();
        }
        return buildErrorResponse(mensaje, HttpStatus.CONFLICT, request);
    }

    // 6. Error de Archivos - CORREGIDO
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleFileStorage(FileStorageException ex, HttpServletRequest request) {
        // Usamos el método buildErrorResponse que ya definiste para mantener la consistencia del JSON
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    // Captura errores de tamaño de Maven/Spring
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildErrorResponse("El archivo excede el límite permitido (Máximo 10MB)", HttpStatus.PAYLOAD_TOO_LARGE, request);
    }

    // 7. El "Caza-todo" (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDTO> handleGlobalException(Exception ex, HttpServletRequest request) {
        // Loguear el error real para el desarrollador, pero ocultarlo al cliente
        log.error("Error interno en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildErrorResponse("Ocurrió un error inesperado en el servidor.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorRespuestaDTO> buildErrorResponse(String mensaje, HttpStatus status, HttpServletRequest request) {
        return new ResponseEntity<>(new ErrorRespuestaDTO(
                LocalDateTime.now(),
                mensaje,
                request.getRequestURI(),
                status.value()
        ), status);
    }
}

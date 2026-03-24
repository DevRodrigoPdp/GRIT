package grit.sistema.backend.exception;

import grit.sistema.backend.dto.ErrorRespuestaDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(
          "Credenciales Inválidas",
          HttpStatus.UNAUTHORIZED,
          request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleAccessDeniedException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleDuplicateKey(DataIntegrityViolationException e, HttpServletRequest request) {
        return buildErrorResponse(
                "Error de Integridad: El registro ya existe o viola una restricción",
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;

        String mensaje = status == HttpStatus.NOT_FOUND ? e.getMessage() : "Error interno en el servidor";

        return buildErrorResponse(mensaje, status, request);
    }

    @ExceptionHandler(UsuarioExistenteException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleUsuarioExistente(UsuarioExistenteException e, HttpServletRequest request) {
        return buildErrorResponse(e.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(SesionActivaException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleSesionActiva(SesionActivaException e, HttpServletRequest request) {
        return buildErrorResponse(e.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errores = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((error) -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String mensaje = String.format("El parámetro '%s' debe ser un UUID válido.", e.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorRespuestaDTO(
                LocalDateTime.now(),
                mensaje,
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()));
    }

    private ResponseEntity<ErrorRespuestaDTO> buildErrorResponse(String mensaje, HttpStatus status, HttpServletRequest request) {
        ErrorRespuestaDTO error = new ErrorRespuestaDTO(
                LocalDateTime.now(),
                mensaje,
                request.getRequestURI(),
                status.value()
        );
        return new ResponseEntity<>(error, status);
    }
}

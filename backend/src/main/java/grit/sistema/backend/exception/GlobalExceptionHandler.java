package grit.sistema.backend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // --- 1. SEGURIDAD (401, 403) ---

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.UNAUTHORIZED, "Credenciales inválidas",
                "El usuario o la contraseña son incorrectos.", request);
    }

    @ExceptionHandler({AccessDeniedException.class, AccesoDenegadoException.class, AuthorizationDeniedException.class})
    public ProblemDetail handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.warn("Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());
        String detail = (ex instanceof TituloFaltanteException) ? ex.getMessage() : "No tiene permisos para ejecutar esta acción.";
        return createProblemDetail(HttpStatus.FORBIDDEN, "Acceso Denegado", detail, request);
    }

    // --- 2. VALIDACIONES Y CLIENTE (400, 405) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST, "Error de Validación",
                "Uno o más campos no cumplen con los requisitos.", request);

        // Formateamos los errores de campo de forma profesional
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(err -> err.getField(), err -> err.getDefaultMessage(), (a, b) -> a));

        pb.setProperty("invalid_params", errors); // Extensión del estándar
        return pb;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST, "Parámetro Faltante",
                "Falta un parámetro requerido en la URL.", request);
        pb.setProperty("parameter_name", ex.getParameterName());
        return pb;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.BAD_REQUEST, "JSON Mal Formado",
                "No se pudo leer el cuerpo de la petición. Verifique la sintaxis JSON.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, "Método No Permitido",
                "El método " + ex.getMethod() + " no está soportado en este endpoint.", request);
    }

    // --- 3. RECURSOS Y CONFLICTOS (404, 409) ---

    @ExceptionHandler({EntityNotFoundException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleNotFound(Exception ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.NOT_FOUND, "Recurso No Encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, UsuarioExistenteException.class, SesionActivaException.class})
    public ProblemDetail handleConflicts(Exception ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.CONFLICT, "Conflicto de Negocio", ex.getMessage(), request);
    }

    // --- 4. ERRORES DE SISTEMA E INFRAESTRUCTURA (500) ---

    @ExceptionHandler({
            DataAccessException.class,
            SQLException.class,
            org.springframework.security.authentication.InternalAuthenticationServiceException.class
    })
    public ProblemDetail handleDatabaseExceptions(Exception ex, HttpServletRequest request) {
        log.error("ERROR CRÍTICO DB en {}: {}", request.getRequestURI(), ex.getMessage());
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error de Persistencia",
                "El servicio de base de datos no está disponible.", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("ERROR NO CONTROLADO en {}: ", request.getRequestURI(), ex);
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno del Servidor",
                "Ha ocurrido un error inesperado en el sistema.", request);
    }

    /**
     * Método utilitario para construir ProblemDetail bajo estándar RFC 7807
     */
    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(status, detail);
        pb.setTitle(title);
        pb.setInstance(URI.create(request.getRequestURI()));
        pb.setProperty("timestamp", LocalDateTime.now()); // Información extra útil
        return pb;
    }
}
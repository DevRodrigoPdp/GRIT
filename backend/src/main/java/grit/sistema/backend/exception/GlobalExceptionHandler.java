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
    private static final String ERR_DOC_URL = "https://api.GRIT.com/errors/";


    // --- 1. SEGURIDAD (401, 403) ---

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.UNAUTHORIZED,
                "authentication-failure",
                "Credenciales inválidas",
                "authentication-failure",
                request);
    }

    @ExceptionHandler({AccessDeniedException.class, AccesoDenegadoException.class, AuthorizationDeniedException.class})
    public ProblemDetail handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.warn("Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());
        String detail = (ex instanceof TituloFaltanteException) ? ex.getMessage() : "No tiene permisos para ejecutar esta acción.";
        return createProblemDetail(HttpStatus.FORBIDDEN, "Acceso Denegado", detail, request);
    }

    // --- 2. VALIDACIONES Y CLIENTE (400, 405) ---

    // --- En Validaciones ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Usamos el slug "validation-error"
        ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST, "Error de Validación",
                "Uno o más campos no cumplen con los requisitos.", "validation-error", request);

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(err -> err.getField(), err -> err.getDefaultMessage(), (a, b) -> a));

        pb.setProperty("invalid_params", errors);
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

    // --- 5. ERRORES ESPECÍFICOS DE VALIDACIÓN Y CARGA ---

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail pb = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Violación de Restricción",
                "Los datos enviados violan las reglas de integridad.",
                "constraint-violation",
                request
        );

        // Extraemos las violaciones de forma legible
        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (existing, replacement) -> existing
                ));

        pb.setProperty("violations", violations);
        return pb;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return createProblemDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Archivo demasiado grande",
                "El archivo excede el límite permitido por el servidor.",
                "file-too-large",
                request
        );
    }

    @ExceptionHandler(RateLimitException.class)
    public ProblemDetail handleRateLimit(RateLimitException ex, HttpServletRequest request) {
        return createProblemDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Demasidas peticiones",
                "Se ha excedido el límite de peticiones.",
                request
        );
    }

    @ExceptionHandler(PwnedPasswordException.class)
    public ProblemDetail handlePwnedPassword(PwnedPasswordException ex, HttpServletRequest request) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Contraseña comprometida",
                "Seguridad insuficiente: Contraseña detectada en filtraciones de datos.",
                request
        );
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
    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String errorSlug,HttpServletRequest request) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(status, detail);
        pb.setType(URI.create(ERR_DOC_URL + errorSlug));
        pb.setTitle(title);
        pb.setInstance(URI.create(request.getRequestURI()));
        pb.setProperty("timestamp", LocalDateTime.now()); // Información extra útil
        return pb;
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, HttpServletRequest request) {
        return createProblemDetail(status, title, detail, status.name().toLowerCase().replace("_", "-"), request);
    }
}
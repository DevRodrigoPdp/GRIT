package grit.sistema.backend.exception;

import grit.sistema.backend.exception.business.BusinessException;
import grit.sistema.backend.exception.business.SesionActivaException;
import grit.sistema.backend.exception.business.TituloFaltanteException;
import grit.sistema.backend.exception.business.UsuarioExistenteException;
import grit.sistema.backend.exception.infrastructure.FileStorageException;
import grit.sistema.backend.exception.infrastructure.RateLimitException;
import grit.sistema.backend.exception.infrastructure.ResourceNotFoundException;
import grit.sistema.backend.exception.security.AccesoDenegadoException;
import grit.sistema.backend.exception.security.AccountNotActiveException;
import grit.sistema.backend.exception.security.PwnedPasswordException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.sql.SQLException;
import java.time.Instant;
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
        log.warn("Credenciales inválidas {}: {}", request.getRequestURI(), ex.getMessage());

        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "authentication-failure",
                "Credenciales inválidas",
                "authentication-failure",
                request);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ProblemDetail handleDisabledAccount(org.springframework.security.authentication.DisabledException ex, HttpServletRequest request) {
        log.warn("Intento de acceso con cuenta desactivada en {}: {}", request.getRequestURI(), ex.getMessage());

        return createProblemDetail(
                HttpStatus.UNAUTHORIZED, // 401: El usuario no puede autenticarse
                "Cuenta Desactivada",
                "Su cuenta ha sido eliminada o suspendida. Póngase en contacto con soporte.",
                "account-disabled", // Slug para la documentación de error
                request
        );
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ProblemDetail handleInternalAuth(InternalAuthenticationServiceException ex, HttpServletRequest request) {
        log.error("Error de autenticación interna: {}", ex.getMessage());
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED, // 401: El usuario no puede autenticarse
                "Cuenta Desactivada",
                "Su cuenta ha sido eliminada o suspendida. Póngase en contacto con soporte.",
                "account-disabled",
                request
        );
    }

    @ExceptionHandler(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class)
    public ProblemDetail handleAuthCredentialsNotFound(Exception ex, HttpServletRequest request) {
        log.warn("No Autenticado {}: {}", request.getRequestURI(), ex.getMessage());
        return createProblemDetail(HttpStatus.UNAUTHORIZED, "No Autenticado", "No se encontraron credenciales de autenticación.", request);
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
        log.warn("Error de Validación {}: {}",
                request.getRequestURI(), ex.getMessage());

        ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST, "Error de Validación",
                "Uno o más campos no cumplen con los requisitos.", "validation-error", request);

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(err -> err.getField(), err -> err.getDefaultMessage(), (a, b) -> a));

        pb.setProperty("invalid_params", errors);
        return pb;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Tipo de Parámetro Incorrecto {}: {}",
                request.getRequestURI(), ex.getMessage());
        String detail = String.format("El parámetro '%s' con valor '%s' no pudo ser convertido al tipo '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Tipo de Parámetro Incorrecto",
                detail,
                "parameter-type-mismatch",
                request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Parámetro Faltante {}: {}",
                request.getRequestURI(), ex.getMessage());
        ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST, "Parámetro Faltante",
                "Falta un parámetro requerido en la URL.", request);
        pb.setProperty("parameter_name", ex.getParameterName());
        return pb;
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ProblemDetail handleMissingPathVariable(MissingPathVariableException ex, HttpServletRequest request) {
        log.warn("Variable de ruta faltante {}: {}",
                request.getRequestURI(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Variable de ruta faltante",
                "Falta la variable: " + ex.getVariableName(),
                "missing-path-variable",
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("JSON Mal Formado {}: {}",
                request.getRequestURI(), ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "JSON Mal Formado",
                "No se pudo leer el cuerpo de la petición. Verifique la sintaxis JSON.", request);
    }

    @ExceptionHandler(com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
    public ProblemDetail handleInvalidFormat(com.fasterxml.jackson.databind.exc.InvalidFormatException ex, HttpServletRequest request) {
        log.warn("Formato JSON Inválido {}: {}",
                request.getRequestURI(), ex.getMessage());
        String detail = String.format("El valor '%s' no es válido para el campo '%s'.",
                ex.getValue(), ex.getPath().get(0).getFieldName());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Formato JSON Inválido", detail, "invalid-format", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Método No Permitido {}: {}",
                request.getRequestURI(), ex.getMessage());
        return createProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, "Método No Permitido",
                "El método " + ex.getMethod() + " no está soportado en este endpoint.", request);
    }

    // --- 3. RECURSOS Y CONFLICTOS (404, 409) ---

    @ExceptionHandler({
            EntityNotFoundException.class,
            ResourceNotFoundException.class
    })
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado {}: {}",
                request.getRequestURI(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado",
                ex.getMessage(),
                "resource-not-found",
                request);
    }

    // 1. CONCURRENCIA: Cuando dos usuarios editan lo mismo
    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLocking(Exception ex, HttpServletRequest request) {
        log.warn("Conflicto de actualización {}: {}",
                request.getRequestURI(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Conflicto de actualización",
                "El registro fue modificado por otro usuario. Por favor, recargue los datos e intente de nuevo.",
                "concurrency-conflict",
                request);
    }

    // 2. ERROR DE ENLACE: Fallos en Query Params o Model Attributes
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ProblemDetail handleBindException(org.springframework.validation.BindException ex, HttpServletRequest request) {
        log.warn("Error de vinculación {}: {}",
                request.getRequestURI(), ex.getMessage());
        ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST, "Error de vinculación",
                "Los parámetros de la petición no son válidos.", "bind-error", request);

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(err -> err.getField(), err -> err.getDefaultMessage(), (a, b) -> a));

        pb.setProperty("invalid_params", errors);
        return pb;
    }

    // 3. RECURSO ESTATICO O RUTA INEXISTENTE (Spring Boot 3.2+)
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Endpoint no encontrado {}: {} - {}",
                request.getRequestURI(),ex.getStatusCode(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Endpoint no encontrado",
                "La ruta solicitada no existe en el servidor.",
                "endpoint-not-found",
                request);
    }

    @ExceptionHandler({UsuarioExistenteException.class, SesionActivaException.class})
    public ProblemDetail handleConflicts(Exception ex, HttpServletRequest request) {
        log.warn("Conflicto de Negocio {}:  {}",
                request.getRequestURI() , ex.getMessage());
        return createProblemDetail(HttpStatus.CONFLICT, "Conflicto de Negocio", ex.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
        String detail = "No se puede realizar la operación debido a que el registro está relacionado con otros datos.";
        log.warn("Conflicto de integridad: {}", ex.getMostSpecificCause().getMessage());
        return createProblemDetail(HttpStatus.CONFLICT, "Conflicto de Integridad", detail, "database-conflict", request);
    }

    @ExceptionHandler(org.springframework.beans.InvalidPropertyException.class)
    public ProblemDetail handleInvalidProperty(org.springframework.beans.InvalidPropertyException ex, HttpServletRequest request) {
        log.warn("Propiedad de Objeto Inválida: {}", ex.getMostSpecificCause().getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Propiedad de Objeto Inválida", ex.getMessage(), request);
    }

    // --- 4. ERRORES DE SISTEMA E INFRAESTRUCTURA (500) ---

    @ExceptionHandler({
            DataAccessException.class,
            SQLException.class
    })
    public ProblemDetail handleDatabaseExceptions(Exception ex, HttpServletRequest request) {
        log.error("ERROR CRÍTICO DB en {}: {}", request.getRequestURI(), ex.getMessage());
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error de Persistencia",
                "El servicio de base de datos no está disponible.", request);
    }

    // --- 5. ERRORES ESPECÍFICOS DE VALIDACIÓN Y CARGA ---

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Violación de Restricción {}:  {}",
                request.getRequestURI() , ex.getMessage());
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
        log.warn("Archivo demasiado grande {}:  {}",
                request.getRequestURI(), ex.getMessage());
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
        log.warn("Se ha excedido el límite de peticiones {}:  {}",
                request.getRequestURI(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Demasidas peticiones",
                "Se ha excedido el límite de peticiones.",
                request
        );
    }

    @ExceptionHandler(PwnedPasswordException.class)
    public ProblemDetail handlePwnedPassword(PwnedPasswordException ex, HttpServletRequest request) {
        log.warn("Contraseña insegura o comprometida {}: {}",
                request.getRequestURI(), ex.getMessage());
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Contraseña comprometida",
                "Seguridad insuficiente: Contraseña detectada en filtraciones de datos.",
                request
        );
    }

    /**
     * Captura específicamente el error de Content-Type incorrecto (415).
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("Content-Type incorrecto {}: {} - {}",
                request.getRequestURI(),ex.getStatusCode(), ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El formato de la petición no es válido."
        );

        problemDetail.setTitle("Tipo de Medio No Soportado");
        problemDetail.setType(URI.create("https://api.tuapp.com/errors/unsupported-media-type"));

        // Añadimos información útil para el desarrollador del frontend
        problemDetail.setProperty("enviado", ex.getContentType());
        problemDetail.setProperty("soportados", ex.getSupportedMediaTypes());
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Captura errores de lógica de negocio personalizados (Ejemplo: Archivo muy grande o error en MinIO).
     */
    @ExceptionHandler(FileStorageException.class)
    public ProblemDetail handleFileStorageException(FileStorageException ex, HttpServletRequest request) {
        log.warn("Archivo demasiado pesado o error en almacenamiento {}:  {}",
                request.getRequestURI(), ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );
        problemDetail.setTitle("Error en Almacenamiento de Archivos");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex, HttpServletRequest request) {
        // 1. Logueamos la advertencia (no es un error de sistema, es una regla de negocio)
        log.warn("Regla de negocio violada en {}: {} - {}",
                request.getRequestURI(), ex.getErrorCode(), ex.getMessage());

        // 2. Determinamos el status.
        // Si es "YA_VINCULADO" podría ser CONFLICT (409), de lo contrario BAD_REQUEST (400)
        HttpStatus status = "YA_VINCULADO".equals(ex.getErrorCode())
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        // 3. Creamos el ProblemDetail usando tu método utilitario
        // Usamos ex.getErrorCode() como el slug para que la URL de documentación sea dinámica
        return createProblemDetail(
                status,
                "Conflicto de Negocio",
                ex.getMessage(),
                ex.getErrorCode().toLowerCase().replace("_", "-"),
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
    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String errorSlug, HttpServletRequest request) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(status, detail);

        String traceId = MDC.get("traceId");

        pb.setType(URI.create(ERR_DOC_URL + errorSlug));
        pb.setTitle(title);
        pb.setInstance(URI.create(request.getRequestURI()));

        pb.setProperty("traceId", traceId != null ? traceId : "N/A");
        pb.setProperty("timestamp", LocalDateTime.now());

        return pb;
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, HttpServletRequest request) {
        return createProblemDetail(status, title, detail, status.name().toLowerCase().replace("_", "-"), request);
    }
}
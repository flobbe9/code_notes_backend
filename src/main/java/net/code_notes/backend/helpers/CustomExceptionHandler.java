package net.code_notes.backend.helpers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.CodeNotesBackendApplication;


/**
 * Class catching any java exception thrown in this api. Will log a shortend stacktrace and return a {@link ResponseEntity} object with a
 * {@link CustomExceptionFormat} object.<p>
 * 
 * Enable TRACE (in .env) to print full stacktrace instead of package only.
 * 
 * @since 0.0.1
 */
@Log4j2
@ControllerAdvice
public class CustomExceptionHandler {

    private static final String INDENT = "     ";


    /**
     * Thrown by {@code @Valid} annotation.
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class) 
    public ResponseEntity<CustomExceptionFormat> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        AtomicReference<String> message = new AtomicReference<>("at " + Utils.getReqeustPath());

        logPackageStackTrace(exception, message.get());

        // log all violations
        exception.getAllErrors().forEach(error -> {
            message.set(error.getDefaultMessage());
            log.error(INDENT + message);
        });

        return getResponse(BAD_REQUEST, message.get());
    }
    
    
    /**
     * Thrown as generic exception with a status code.
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(value = ResponseStatusException.class)
    public ResponseEntity<CustomExceptionFormat> handleException(ResponseStatusException exception) {

        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String  message = Utils.isBlank(exception.getReason()) ? status.getReasonPhrase() : exception.getReason();

        logPackageStackTrace(exception, message);

        return getResponse(status, message);
    }
        

    /**
     * Thrown as generic exception with a status code.
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<CustomExceptionFormat> handleException(IllegalArgumentException exception) {

        logPackageStackTrace(exception, exception.getMessage());

        return getResponse(BAD_REQUEST, exception.getMessage());
    }


    /**
     * Thrown by annotations like{@code @NotNull} etc. (?).
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(value = HandlerMethodValidationException.class) 
    public ResponseEntity<CustomExceptionFormat> handleException(HandlerMethodValidationException exception) {

        AtomicReference<String> message = new AtomicReference<>("at " + Utils.getReqeustPath());

        logPackageStackTrace(exception, message.get());

        // log all violations
        exception.getAllErrors().forEach(error -> {
            message.set(error.getDefaultMessage());
            log.error(INDENT + message);
        });
        
        return getResponse(BAD_REQUEST, message.get());
    }

        
    /**
     * Thrown when a http request fails with a 4xx status that was sent from the backend, e.g. using {@code RestClient}
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<CustomExceptionFormat> handleException(HttpClientErrorException exception) {

        logPackageStackTrace(exception);

        CustomExceptionFormat customExceptionFormat = exception.getResponseBodyAs(CustomExceptionFormat.class);

        return getResponse(exception.getStatusCode(), customExceptionFormat != null ? exception.getMessage() : exception.getStatusText());
    }
    
        
    /**
     * Thrown when a http request fails with a 5xx status that was sent from the backend, e.g. using {@code RestClient}
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(value = HttpServerErrorException.class)
    public ResponseEntity<CustomExceptionFormat> handleException(HttpServerErrorException exception) {

        logPackageStackTrace(exception);

        CustomExceptionFormat customExceptionFormat = exception.getResponseBodyAs(CustomExceptionFormat.class);

        return getResponse(exception.getStatusCode(), customExceptionFormat != null ? exception.getMessage() : exception.getStatusText());
    }

        
    /**
     * Set status to 403 with a simple message.
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(value = AuthorizationDeniedException.class)
    public ResponseEntity<CustomExceptionFormat> handleException(AuthorizationDeniedException exception) {

        logPackageStackTrace(exception);

        return getResponse(HttpStatus.FORBIDDEN);
    }

    
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<CustomExceptionFormat> handleException(Exception exception) {

        logPackageStackTrace(exception);

        return getResponse(INTERNAL_SERVER_ERROR, exception.getMessage());
    }


    /**
     * @param statusCode http response status code
     * @param message either a custom error message or (if null) the status reason phrase will be used
     * @return the default exception response
     */
    public static ResponseEntity<CustomExceptionFormat> getResponse(int statusCode, @Nullable String message) {

        return getResponse(HttpStatus.valueOf(statusCode), message);
    }


    /**
     * Will use the {@code status} reason phrase as message.
     * 
     * @param status http response status code
     * @return the default exception response
     */
    public static ResponseEntity<CustomExceptionFormat> getResponse(HttpStatusCode status) {

        return getResponse(HttpStatus.valueOf(status.value()), null);
    }


    /**
     * @param status http response status code
     * @param message either a custom error message or (if null) the status reason phrase will be used
     * @return the default exception response
     */
    public static ResponseEntity<CustomExceptionFormat> getResponse(HttpStatusCode status, @Nullable String message) {

        return getResponse(HttpStatus.valueOf(status.value()), message);
    }


    /**
     * @param status http response status code
     * @param message either a custom error message or (if null) the status reason phrase will be used
     * @return the default exception response
     */
    public static ResponseEntity<CustomExceptionFormat> getResponse(HttpStatus status, @Nullable String message) {

        return
            ResponseEntity
                .status(status.value())
                .body(
                    new CustomExceptionFormat(
                        status.value(),
                        Utils.isBlank(message) ? status.getReasonPhrase() : message
                    ));
    }


    /**
     * Logs and formats parts of given {@code throwable} and it's cause that include classes of the {@link CodeNotesBackendApplication} package (e.g. com.example...) but will 
     * exclude any other package (like java.lang etc.).<p>
     * 
     * Will log the message before the stacktrace if not null
     * 
     * @param throwable to take the stack trace from
     * @param message error message to log in front of stacktrace
     */
    public static void logPackageStackTrace(@Nullable Throwable throwable, @Nullable String message) {
        
        if (throwable == null)
            return;
        
        log.error(throwable.getClass().getName() + ": " + (Utils.isBlank(message) ? "" : message));

        Arrays.asList(throwable.getStackTrace()).forEach(trace -> {
            if (isPackageStackTrace(trace)) 
                log.error(INDENT + "at " + trace.getClassName() + "." + trace.getMethodName() + "(" + trace.getFileName() + ":" + trace.getLineNumber() + ")");
        });
        
        // cause
        if (throwable.getCause() != null) {
            log.error("Caused by:");
            logPackageStackTrace(throwable.getCause());
        }

        if (log.isTraceEnabled())
            throwable.printStackTrace();
    }


    public static void logPackageStackTrace(@Nullable Throwable throwable) {

        logPackageStackTrace(throwable, throwable.getMessage());
    }
    

    /**
     * Checks if given {@link StackTraceElement} references a class of the {@link CodeNotesBackendApplication} package.
     * 
     * @param trace to check
     * @return true if referenced class is in {@link CodeNotesBackendApplication} package
     */
    private static boolean isPackageStackTrace(StackTraceElement trace) {

        return trace.getClassName().startsWith(CodeNotesBackendApplication.class.getPackage().getName());
    }       
}
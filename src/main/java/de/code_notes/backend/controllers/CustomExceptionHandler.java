package de.code_notes.backend.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import de.code_notes.backend.CodeNotesBackendApplication;
import de.code_notes.backend.helpers.Utils;
import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;


/**
 * Class catching any java exception thrown in this api. Will log a shortend stacktrace and return a {@link ResponseEntity} object with a
 * {@link CustomExceptionFormat} object.
 * 
 * @since 0.0.1
 */
@Log4j2
@ControllerAdvice
@Validated
public class CustomExceptionHandler {

    private final String indent = "     ";


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
            log.error(this.indent + message);
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

        logPackageStackTrace(exception, exception.getReason());

        return getResponse(HttpStatus.valueOf(exception.getStatusCode().value()), exception.getReason());
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
            log.error(this.indent + message);
        });
        
        return getResponse(BAD_REQUEST, message.get());
    }

        
    /**
     * Set status to 403 with a simple message. Don't log error.
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(value = AuthorizationDeniedException.class)
    public ResponseEntity<CustomExceptionFormat> handleException(AuthorizationDeniedException exception) {

        return getResponse(HttpStatus.FORBIDDEN, "Forbidden");
    }

    
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<CustomExceptionFormat> handleException(Exception exception) {

        logPackageStackTrace(exception);

        return getResponse(INTERNAL_SERVER_ERROR, exception.getMessage());
    }


    /**
     * @param status http response status code
     * @param message
     * @return the default exception response
     */
    private ResponseEntity<CustomExceptionFormat> getResponse(HttpStatus status, String message) {

        return ResponseEntity.status(status.value())
                             .body(new CustomExceptionFormat(
                                Utils.formatLocalDateTimeDefault(LocalDateTime.now()),
                                status.value(),
                                message,
                                Utils.getReqeustPath()
                            ));
    }


    /**
     * Logs and formats parts of given stacktrace array that include classes of the {@link CodeNotesBackendApplication} package (e.g. com.example...) but will 
     * exclude any other package (like java.lang etc.).
     * 
     * Will log the message before the stacktrace if not null
     * 
     * @param exception to take the stack trace from
     * @param message exception message to log in front of stacktrace. May be null
     */
    private void logPackageStackTrace(Exception exception, @Nullable String message) {
        
        log.error(exception.getClass().getName() + ": " + (Utils.isBlank(message) ? "" : message));

        Arrays.asList(exception.getStackTrace()).forEach(trace -> {
            if (isPackageStackTrace(trace)) 
                log.error(this.indent + "at " + trace.getClassName() + "." + trace.getMethodName() + "(" + trace.getFileName() + ":" + trace.getLineNumber() + ")");
        });
    }


    private void logPackageStackTrace(Exception exception) {

        logPackageStackTrace(exception, exception.getMessage());
    }
    

    /**
     * Checks if given {@link StackTraceElement} references a class of the {@link CodeNotesBackendApplication} package.
     * 
     * @param trace to check
     * @return true if referenced class is in {@link CodeNotesBackendApplication} package
     */
    private boolean isPackageStackTrace(StackTraceElement trace) {

        return trace.getClassName().startsWith(CodeNotesBackendApplication.class.getPackage().getName());
    }       
}
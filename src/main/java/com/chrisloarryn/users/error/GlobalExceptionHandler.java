package com.chrisloarryn.users.error;

import java.net.URI;
import java.util.List;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PERSISTENCE_CONFLICT_MESSAGE =
            "The request could not be completed because it violates persisted data constraints.";
    private static final String INTERNAL_ERROR_MESSAGE =
            "The service could not complete the operation. Please try again later.";

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", List.of(exception.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(ConflictException exception) {
        return problem(HttpStatus.CONFLICT, "Conflict", List.of(exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", List.of(exception.getMessage()));
    }

    @ExceptionHandler(BusinessValidationException.class)
    ProblemDetail handleBusinessValidation(BusinessValidationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Business Rule Violation", List.of(exception.getMessage()));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class
    })
    ProblemDetail handleBadRequest(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", List.of(exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", List.of("Malformed request body."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", List.of("Invalid path or query parameter value."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                        : error.getDefaultMessage())
                .toList();
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        LOGGER.warn("Persistence constraint violation", exception);
        return problem(HttpStatus.CONFLICT, "Conflict", List.of(PERSISTENCE_CONFLICT_MESSAGE));
    }

    @ExceptionHandler({
            DataAccessException.class,
            JpaSystemException.class,
            TransactionSystemException.class
    })
    ProblemDetail handlePersistenceFailure(Exception exception) {
        LOGGER.error("Persistence operation failed", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", List.of(INTERNAL_ERROR_MESSAGE));
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        LOGGER.error("Unhandled application error", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", List.of(INTERNAL_ERROR_MESSAGE));
    }

    private ProblemDetail problem(HttpStatus status, String title, List<String> errors) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, errors.isEmpty() ? title : errors.get(0));
        detail.setTitle(title);
        detail.setType(URI.create("about:blank"));
        detail.setProperty("errors", errors);
        return detail;
    }
}

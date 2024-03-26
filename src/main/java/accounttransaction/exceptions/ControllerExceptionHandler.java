package accounttransaction.exceptions;

import accounttransaction.entities.UserNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
@Order(1)
@ResponseBody
public class ControllerExceptionHandler { // extends ResponseEntityExceptionHandler {
    private static final Map<String, String> typeToErrorMessage = new HashMap<>();

    static {
        typeToErrorMessage.put("java.time.LocalDate", "La fecha debe tener el formato 'yyyy-MM-dd'");
        typeToErrorMessage.put("java.util.UUID", "El id debe tener el formato 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'");
        typeToErrorMessage.put("java.lang.Double", "El monto debe ser un número decimal");
        // Añadir más mapeos según sea necesario
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex) {
        String message = "No se encontró el usuario solicitado.";
        ApiErrorResponse apiError = new ApiErrorResponse(
                HttpStatus.NOT_FOUND,
                message,
                Collections.singletonList(ex.getMessage())
        );

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    // 401
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException ex) {
        String message = "Usuario no autorizado.";
        ApiErrorResponse apiError = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED,
                message,
                Collections.singletonList(ex.getMessage())
        );

        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InsuficientBalanceException.class)
    public ResponseEntity<Object> handleSaldoInsuficiente(InsuficientBalanceException ex) {
        String message = "El usuario no tiene suficiente saldo para completar la transacción.";
        ApiErrorResponse apiError = new ApiErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                message,
                Collections.singletonList(ex.getMessage())
        );

        return new ResponseEntity<>(apiError, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(UnparseableDateException.class)
    public ResponseEntity<Object> handleUnparseableDate(UnparseableDateException ex) {
        String message = "La fecha no pudo ser interpretada. Asegúrate de que tenga el formato 'yyyy-MM-dd'.";
        ApiErrorResponse apiError = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                Collections.singletonList(ex.getMessage())
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String readableMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        String captureField = extractFieldName(readableMessage);
        String errorType = extractErrorType(readableMessage);

        String errorMessage = typeToErrorMessage.getOrDefault(errorType, "La solicitud contiene campos con formato inválido o inesperado. Verifica la documentación.");

        List<String> errors = Collections.singletonList(errorMessage);

        ApiErrorResponse apiError = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Error de formato en la solicitud: " + captureField,
                errors);

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    private String extractFieldName(String message) {
        Pattern pattern = Pattern.compile("from String \"(.*?)\":");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : "desconocido";
    }

    private String extractErrorType(String message) {
        Pattern pattern = Pattern.compile("type `(.+?)`");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : "desconocido";
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.BAD_REQUEST);
        BadRequestException body = BadRequestException.builder()
                .message(ex.getMessage())
                .code(ex.getCode())
                .build();

        return builder.body(body);
    }
}

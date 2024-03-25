package accounttransaction.exceptions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String message;
    private final String code;

    public CustomException(String message, String code) {
        super(message);
        this.message = message;
        this.code = code;
    }
}

package service.exceptions;

/**
 * Thrown when a transaction Id cannot be found.
 *
 * Created by simone on 13/02/16.
 */
public class TransactionNotFoundException extends RuntimeException {
    private String message;

    public TransactionNotFoundException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

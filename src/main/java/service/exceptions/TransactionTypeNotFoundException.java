package service.exceptions;

/**
 * Thrown when a transaction type does not exists.
 *
 * Created by simone on 13/02/16.
 */
public class TransactionTypeNotFoundException extends RuntimeException {
    private String message;

    public TransactionTypeNotFoundException(String message) {
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

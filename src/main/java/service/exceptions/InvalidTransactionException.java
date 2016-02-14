package service.exceptions;

import model.Transaction;

import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

/**
 * Thrown when trying to insert a transaction which is invalid.
 *
 * Created by simone on 13/02/16.
 */
public class InvalidTransactionException extends RuntimeException {

    private Set<ConstraintViolation<Transaction>> errors = new HashSet<>();
    private String message;

    public InvalidTransactionException(Set<ConstraintViolation<Transaction>> errors) {
        this.errors = errors;
    }
    public InvalidTransactionException(String message) {
        this.message = message;
    }


    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Set<ConstraintViolation<Transaction>> getErrors() {
        return errors;
    }

    public void setErrors(Set<ConstraintViolation<Transaction>> errors) {
        this.errors = errors;
    }

}

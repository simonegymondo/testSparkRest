package service;

import model.Transaction;
import model.TransactionType;
import org.springframework.stereotype.Service;
import service.exceptions.InvalidTransactionException;
import service.exceptions.TransactionNotFoundException;

import javax.validation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple CRUD for {@link model.Transaction}
 *
 * Created by simone on 13/02/16.
 */
@Service
public class TransactionService {

    /**
     * Stores the transaction data.
     */
    private Map<Long, Transaction> storage = new HashMap<>();
    /**
     * Maps the type to transaction entities.
     */
    private Map<TransactionType, Set<Transaction>> typeIndex = new HashMap<>();

    private Validator validator;

    public TransactionService() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Insert a transaction into the storage.
     * @param transaction
     */
    public synchronized Transaction insert(Transaction transaction) {
        Objects.requireNonNull(transaction);
        Set<ConstraintViolation<Transaction>> validation = validator.validate(transaction);

        if (validation.size() > 0) {
            throw new InvalidTransactionException(validation);
        }

        // Copy the object to simulate a persistence layer
        Transaction entity = new Transaction(transaction);

        // Add to the parent sum.
        if (transaction.getParentId() != null) {
            if (transaction.getParentId() == transaction.getId()) {
                throw new InvalidTransactionException("Parent id is equal to transaction id");
            }

            Transaction parentEntity = getEntity(transaction.getParentId());
            parentEntity.addToSumOfChildren(transaction.getAmount());
            Transaction existingTransaction = storage.get(transaction.getId());
            if (existingTransaction != null) {
                parentEntity.addToSumOfChildren(-existingTransaction.getAmount());
            }
        }

        // Insert into the storage.
        storage.put(transaction.getId(), entity);

        // Add to the type index.
        if (typeIndex.get(transaction.getTransactionType()) == null) {
            typeIndex.put(transaction.getTransactionType(), new HashSet<>());
        }
        typeIndex.get(transaction.getTransactionType()).add(entity);

        return transaction;
    }

    /**
     * Returns the transaction entity.
     *
     * @param id
     * @return
     */
    private Transaction getEntity(Long id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(storage.get(id))
                .orElseThrow(() ->
                        new TransactionNotFoundException("Transaction id " + String.valueOf(id) + " not found."));
    }

    /**
     * Fetches a transaction of the given id.
     * @param id
     */
    public synchronized Transaction get(Long id) {
        return getEntity(id);
    }

    /**
     * Sums the amount of {@link Transaction} with same parent.
     * @param transactionId
     * @return
     */
    public synchronized Double sumSiblings(Long transactionId) {
        Objects.requireNonNull(transactionId);
        Transaction transaction = get(transactionId);

        // No parent
        if (transaction.getParentId() == null) {
            return .0;
        }

        return getEntity(transaction.getParentId()).getSumOfChildren();
    }

    /**
     * Get list of transactions by type.
     *
     * @param transactionType
     * @return
     */
    public synchronized List<Long> getByType(TransactionType transactionType) {
        Objects.requireNonNull(transactionType);
        return typeIndex.get(transactionType).stream()
                .map(Transaction::getId)
                .collect(Collectors.toList());
    }
}

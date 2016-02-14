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
     * Stores the {@link Transaction} data.
     */
    private Map<Long, Transaction> storage = new HashMap<>();
    /**
     * Maps a type to {@link Transaction} entities.
     */
    private Map<TransactionType, Set<Long>> typeIndex = new HashMap<>();
    /**
     * Hibernate validators.
     */
    private Validator validator;

    public TransactionService() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Insert a {@link Transaction} into the storage.
     * @param transaction
     */
    public synchronized Transaction insert(Transaction transaction) {
        Set<ConstraintViolation<Transaction>> validation = validator.validate(transaction);

        if (validation.size() > 0) {
            throw new InvalidTransactionException(validation);
        }

        // not null if a transaction with same id already exists.
        final Transaction existingTransaction = storage.get(transaction.getId());
        // Copy the object to have persistence.
        final Transaction entity = new Transaction(transaction);

        // Remove previous data.
        if (existingTransaction != null) {
            if (existingTransaction.getParentId() != null) {
                getEntity(existingTransaction.getParentId()).addToSumOfChildren(-existingTransaction.getAmount());
            }
            typeIndex.get(existingTransaction.getTransactionType()).remove(existingTransaction.getId());
        }

        // Add to the parent sum.
        if (transaction.getParentId() != null) {
            if (transaction.getParentId().equals(transaction.getId())) {
                throw new InvalidTransactionException("Parent id is equal to transaction id.");
            }

            final Transaction parentEntity = getEntity(transaction.getParentId());
            if (parentEntity != null
                    && parentEntity.getParentId() != null
                    && parentEntity.getParentId().equals(transaction.getId())) {
                throw new InvalidTransactionException("Cyclic reference. The parent id of this transaction points to on of its children");
            }
            parentEntity.addToSumOfChildren(transaction.getAmount());
        }

        // Insert into the storage.
        storage.put(transaction.getId(), entity);

        // Add to the type index.
        if (typeIndex.get(transaction.getTransactionType()) == null) {
            typeIndex.put(transaction.getTransactionType(), new HashSet<>());
        }
        typeIndex.get(transaction.getTransactionType()).add(entity.getId());

        return transaction;
    }

    /**
     * Returns the {@link Transaction} entity.
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
     * Fetches a {@link Transaction} of the given id.
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
    public synchronized Double getChildrenSum(Long transactionId) {
        Objects.requireNonNull(transactionId);
        return get(transactionId).getSumOfChildren();
    }

    /**
     * Get list of {@link Transaction} by type.
     *
     * @param transactionType
     * @return
     */
    public synchronized Set<Long> getByType(TransactionType transactionType) {
        Objects.requireNonNull(transactionType);
        return typeIndex.get(transactionType);
    }
}

package unit;

import model.Transaction;
import model.TransactionType;
import org.junit.BeforeClass;
import org.junit.Test;
import service.TransactionService;
import service.exceptions.InvalidTransactionException;
import service.exceptions.TransactionNotFoundException;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests for the transaction service.
 *
 * Created by simone on 13/02/16.
 */
public class TransactionServiceTest {

    TransactionService transactionService = new TransactionService();


    @Test(expected = InvalidTransactionException.class)
    public void invalidTransactionTest() {
        Transaction transaction = new Transaction(null, null, null, null);
        transactionService.insert(transaction);
    }

    @Test
    public void transactionInsertionTest() {
        Transaction transaction = new Transaction(1L, 10.0, null, TransactionType.CARS);

        transactionService.insert(transaction);
        assertEquals((Long)1L, transactionService.get(1L).getId());
        assertTrue(transaction != transactionService.get(1L));
    }

    @Test
    public void transactionInsertionUpdatedTest() {
        Transaction transaction = new Transaction(1L, 10.0, null, TransactionType.CARS);
        transactionService.insert(transaction);
        assertEquals((Long)1L, transactionService.get(1L).getId());
        transaction.setAmount(20.0);
        transactionService.insert(transaction);
        assertEquals(20.0, transactionService.get(1L).getAmount());
    }

    @Test
    public void transactionSumTest() {
        Transaction transaction1 = new Transaction(1L, 10.0, null, TransactionType.CARS);
        Transaction transaction2 = new Transaction(2L, 10.0, 1L, TransactionType.CARS);
        Transaction transaction3 = new Transaction(3L, 10.0, 1L, TransactionType.CARS);

        transactionService.insert(transaction1);
        transactionService.insert(transaction2);
        transactionService.insert(transaction3);

        assertEquals(20.0, transactionService.sumSiblings(2L));
    }

    @Test
    public void transactionSumUpdateTest() {
        Transaction transaction1 = new Transaction(1L, 10.0, null, TransactionType.CARS);
        Transaction transaction2 = new Transaction(2L, 10.0, 1L, TransactionType.CARS);
        Transaction transaction3 = new Transaction(3L, 10.0, 1L, TransactionType.CARS);

        transactionService.insert(transaction1);
        transactionService.insert(transaction2);
        transactionService.insert(transaction3);

        assertEquals(20.0, transactionService.sumSiblings(2L), 20.0);
        assertEquals(20.0, transactionService.sumSiblings(3L), 20.0);
        Transaction transaction4 = new Transaction(2L, 20.0, 1L, TransactionType.CARS);
        transactionService.insert(transaction4);
        assertEquals(30.0, transactionService.sumSiblings(3L));
    }

    @Test(expected = InvalidTransactionException.class)
    public void transactionInsertInvalidParent() {
        Transaction transaction = new Transaction(1L, 10.0, 1L, TransactionType.CARS);
        transactionService.insert(transaction);
    }

    @Test(expected = TransactionNotFoundException.class)
    public void transactionNonExistingParent() {
        Transaction transaction = new Transaction(1L, 10.0, 2L, TransactionType.CARS);
        transactionService.insert(transaction);
    }

    @Test()
    public void transactionSameType() {
        Transaction transaction1 = new Transaction(1L, 10.0, null, TransactionType.CARS);
        Transaction transaction2 = new Transaction(2L, 10.0, null, TransactionType.CARS);
        Transaction transaction3 = new Transaction(3L, 10.0, null, TransactionType.SHOPPING);
        transactionService.insert(transaction1);
        transactionService.insert(transaction2);
        transactionService.insert(transaction3);

        assertTrue(new HashSet(transactionService.getByType(TransactionType.CARS))
                .containsAll(new HashSet(Arrays.asList(1L, 2L))));
        assertTrue(new HashSet(transactionService.getByType(TransactionType.SHOPPING))
                .containsAll(new HashSet(Arrays.asList(3L))));
    }

    @Test()
    public void transactionSameTypeIncludedOnce() {
        Transaction transaction1 = new Transaction(1L, 10.0, null, TransactionType.CARS);
        Transaction transaction2 = new Transaction(1L, 20.0, null, TransactionType.CARS);

        transactionService.insert(transaction1);
        transactionService.insert(transaction2);

        assertTrue(new HashSet(transactionService.getByType(TransactionType.CARS))
                .containsAll(new HashSet(Arrays.asList(1L))));
    }
}


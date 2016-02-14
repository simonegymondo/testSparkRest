package unit;

import model.Transaction;
import model.TransactionType;
import org.junit.Test;
import service.TransactionService;
import service.exceptions.InvalidTransactionException;
import service.exceptions.TransactionNotFoundException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    @Test(expected = InvalidTransactionException.class)
    public void nullAmountTransactionTest() {
        Transaction transaction = new Transaction(1L, null, null, TransactionType.CARS);
        transactionService.insert(transaction);
    }

    @Test
    public void transactionInsertionTest() {
        Transaction transaction = new Transaction(1L, 10.0, null, TransactionType.CARS);

        transactionService.insert(transaction);
        assertEquals((Long)1L, transactionService.get(1L).getId());
        assertEquals(10.0, transactionService.get(1L).getAmount());
        assertEquals(null, transactionService.get(1L).getParentId());
        assertEquals(TransactionType.CARS, transactionService.get(1L).getTransactionType());
        assertTrue(transaction != transactionService.get(1L));
    }

    @Test(expected = InvalidTransactionException.class)
    public void transactionCyclicInsertionTest() {
        Transaction transaction = new Transaction(1L, 10.0, null, TransactionType.CARS);
        Transaction transaction2 = new Transaction(2L, 10.0, 1L, TransactionType.CARS);
        transactionService.insert(transaction);
        transactionService.insert(transaction2);
        transaction.setParentId(2L);
        transactionService.insert(transaction);
    }

    @Test
    public void transactionInsertionUpdatedTest() {
        Transaction transaction = new Transaction(1L, 10.0, null, TransactionType.CARS);
        transactionService.insert(transaction);
        assertEquals(10.0, transactionService.get(1L).getAmount());
        assertEquals((Long)1L, transactionService.get(1L).getId());
        transaction.setAmount(20.0);
        transactionService.insert(transaction);
        assertEquals(20.0, transactionService.get(1L).getAmount());
    }

    @Test
    public void transactionMultiInsertionAndSumTest() {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, 1L, TransactionType.CARS));

        assertEquals((Long)2L, transactionService.get(2L).getId());
        assertEquals(10.0, transactionService.get(2L).getAmount());
        assertEquals((Long)1L, transactionService.get(2L).getParentId());
        assertEquals(TransactionType.CARS, transactionService.get(2L).getTransactionType());

        assertEquals(0.0, transactionService.getChildrenSum(2L));
        assertEquals(20.0, transactionService.getChildrenSum(1L));
    }

    @Test
    public void transactionNegativeSumTest() {
        transactionService.insert(new Transaction(1L, -10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 120.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, -310.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(4L, -310.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(5L, -310.0, 1L, TransactionType.CARS));

        assertEquals(-810.0, transactionService.getChildrenSum(1L));
    }

    @Test
    public void transactionSumUpdateTest() {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, 1L, TransactionType.CARS));

        assertEquals(20.0, transactionService.getChildrenSum(2L), 20.0);
        assertEquals(20.0, transactionService.getChildrenSum(3L), 20.0);
        transactionService.insert(new Transaction(2L, 20.0, 1L, TransactionType.CARS));

        assertEquals(30.0, transactionService.getChildrenSum(1L));
    }

    @Test
    public void transactionSumUpdateParentIdTest() {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, 1L, TransactionType.CARS));

        assertEquals(20.0, transactionService.getChildrenSum(2L), 20.0);
        assertEquals(20.0, transactionService.getChildrenSum(3L), 20.0);
        transactionService.insert(new Transaction(2L, 20.0, 3L, TransactionType.CARS));

        assertEquals(10.0, transactionService.getChildrenSum(1L));
        assertEquals(20.0, transactionService.getChildrenSum(3L));
    }

    @Test
    public void transactionSumUpdateParentIdFromNullTest() {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, 1L, TransactionType.CARS));

        assertEquals(20.0, transactionService.getChildrenSum(2L), 20.0);
        assertEquals(20.0, transactionService.getChildrenSum(3L), 20.0);
        transactionService.insert(new Transaction(2L, 20.0, null, TransactionType.CARS));

        assertEquals(10.0, transactionService.getChildrenSum(1L));
    }

    @Test
    public void transactionSumNegativeUpdateTest() {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, 1L, TransactionType.CARS));

        assertEquals(20.0, transactionService.getChildrenSum(2L), 20.0);
        assertEquals(20.0, transactionService.getChildrenSum(3L), 20.0);
        transactionService.insert(new Transaction(2L, -20.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, 1L, TransactionType.CARS));

        assertEquals(-10.0, transactionService.getChildrenSum(1L));
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

    @Test
    public void transactionSameType() {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, null, TransactionType.SHOPPING));

        compareSets(new HashSet(Arrays.asList(1L, 2L)), new HashSet<>(transactionService.getByType(TransactionType.CARS)));
        compareSets(new HashSet(Arrays.asList(3L)), new HashSet<>(transactionService.getByType(TransactionType.SHOPPING)));
    }

    @Test
    public void transactionSameTypeUpdateType() {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, null, TransactionType.SHOPPING));

        compareSets(new HashSet(Arrays.asList(1L, 2L)), new HashSet<>(transactionService.getByType(TransactionType.CARS)));
        compareSets(new HashSet(Arrays.asList(3L)), new HashSet<>(transactionService.getByType(TransactionType.SHOPPING)));

        transactionService.insert(new Transaction(3L, 10.0, null, TransactionType.CARS));

        compareSets(new HashSet(Arrays.asList(1L, 2L, 3L)), new HashSet(transactionService.getByType(TransactionType.CARS)));
        compareSets(new HashSet(Arrays.asList()), new HashSet(transactionService.getByType(TransactionType.SHOPPING)));
    }

    @Test
    public void transactionSameTypeIncludedOnce() {
        Transaction transaction1 = new Transaction(1L, 10.0, null, TransactionType.CARS);
        Transaction transaction2 = new Transaction(1L, 20.0, null, TransactionType.CARS);

        transactionService.insert(transaction1);
        transactionService.insert(transaction2);

        compareSets(new HashSet(Arrays.asList(1L)), new HashSet(transactionService.getByType(TransactionType.CARS)));
    }

    /**
     * Compare two sets by size and content.
     * @param set1
     * @param set2
     */
    private void compareSets(Set<?> set1, Set<?> set2) {
        assertEquals(set1.size(), set2.size());
        assertTrue(set1.containsAll(set2));
    }
}


package model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Encloses a {@link Transaction} and some additional data.
 *
 * Created by simone on 13/02/16.
 */
public class Transaction {
    /**
     * The list of children transactions.
     */
    @JsonIgnore
    private List<Transaction> transactionList = new ArrayList<>();
    /**
     * Sum of children
     */
    @JsonIgnore
    private Double sumOfChildren = .0;


    @NotNull
    @Min(0L)
    private Long id;
    /**
     * An amount in an unknown currency
     */
    @NotNull
    private Double amount;
    /**
     * A parent transaction if existing
     */
    @Min(0L)
    private Long parentId;
    /**
     * The type of transaction
     */
    @NotNull
    private TransactionType transactionType;

    public Transaction() {

    }

    public Transaction(Long id, Double amount, Long parentId, TransactionType transactionType) {
        this.id = id;
        this.amount = amount;
        this.parentId = parentId;
        this.transactionType = transactionType;
    }

    public Transaction(Transaction other) {
        this.id = other.id;
        this.amount = other.amount;
        this.parentId = other.parentId;
        this.transactionType = other.transactionType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }


    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public void addTransactionList(Transaction transaction) {
        this.transactionList.add(transaction);
    }

    public Double getSumOfChildren() {
        return sumOfChildren;
    }

    public void setSumOfChildren(Double sumOfChildren) {
        this.sumOfChildren = sumOfChildren;
    }

    public void addToSumOfChildren(Double sumOfChildren) {
        this.sumOfChildren += sumOfChildren;
    }
}

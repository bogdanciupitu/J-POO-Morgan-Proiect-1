package org.poo.classes;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Account {
    private String currency;
    private String accountType;
    private int timestamp;
    private double balance;
    private String iban;
    private ArrayList<Card> cards;
    private double minBalance;
    private ArrayList<Transaction> transactions;

    public Account(final String currency, final String accountType, final int timestamp,
                   final double balance, final String iban, final ArrayList<Card> cards,
                   final ArrayList<Transaction> transactions) {
        this.currency = currency;
        this.accountType = accountType;
        this.timestamp = timestamp;
        this.balance = balance;
        this.iban = iban;
        this.cards = cards;
        this.transactions = transactions;
        this.minBalance = 0;
    }
}

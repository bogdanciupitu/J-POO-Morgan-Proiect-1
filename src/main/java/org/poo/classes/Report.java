package org.poo.classes;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Report {
    private String iban;
    private double balance;
    private String currency;
    private ArrayList<Transaction> transactions;

    public Report(final String iban, final double balance, final String currency,
                  final ArrayList<Transaction> transactions) {
        this.iban = iban;
        this.balance = balance;
        this.currency = currency;
        this.transactions = transactions;
    }
}

package org.poo.classes;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final String currency, final String accountType, final int timestamp,
                          final double balance, final String iban, final double interestRate,
                          final ArrayList<Card> cards) {
        super(currency, accountType, timestamp, balance, iban, cards, new ArrayList<>());
        this.interestRate = interestRate;
    }
}

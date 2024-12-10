package org.poo.classes;

import java.util.ArrayList;

public class AccountFactory {
    public static Account createAccount(final String currency, final String accountType,
                                        final int timestamp, final String iban,
                                        final double interestRate) {
        switch (accountType) {
            case "classic":
                return new ClassicAccount(currency, accountType, timestamp, 0, iban,
                        new ArrayList<>());
            case "savings":
                return new SavingsAccount(currency, accountType, timestamp, 0, iban, interestRate,
                        new ArrayList<>());
            default:
                return null;
        }
    }
}

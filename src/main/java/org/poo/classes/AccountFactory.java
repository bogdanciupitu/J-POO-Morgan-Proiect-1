package org.poo.classes;

import java.util.ArrayList;

public final class AccountFactory {
    private AccountFactory() {
    }

    /**
     *  Factory pattern to create 2 types of accounts
     *
     * @param currency the currency of the account
     * @param accountType the type of the account which can be classic or savings
     * @param timestamp the timestamp of the account creation
     * @param iban the IBAN of the account
     * @param interestRate the interest rate of the savings accounts
     * @return the created account
     */
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

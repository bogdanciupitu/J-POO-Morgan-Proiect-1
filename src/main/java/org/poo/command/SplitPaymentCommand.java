package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.fileio.CommandInput;

import java.util.HashSet;
import java.util.List;

public final class SplitPaymentCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        List<String> accountsForSplit = command.getAccounts();
        int timestamp = command.getTimestamp();
        String currency = command.getCurrency();
        double amount = command.getAmount();

        double splitAmount = amount / accountsForSplit.size();
        boolean canSplit = true;

        String insufficientFundsAccount = null;
        for (String accountIBAN : accountsForSplit) {
            Account account = bank.findAccountByIBAN(accountIBAN);
            if (account != null) {
                double conversionRate = bank.convert(currency, account.getCurrency(),
                        new HashSet<>());
                double convertedSplitAmount = splitAmount * conversionRate;

                if (account.getBalance() < convertedSplitAmount) {
                    canSplit = false;
                    insufficientFundsAccount = accountIBAN;
                }
            }
        }

        for (String accountIBAN : accountsForSplit) {
            Account account = bank.findAccountByIBAN(accountIBAN);
            if (account != null) {
                if (canSplit) {
                    double conversionRate = bank.convert(currency, account.getCurrency(),
                            new HashSet<>());
                    double convertedAmount = splitAmount * conversionRate;
                    account.setBalance(account.getBalance() - convertedAmount);

                    String description = String.format("Split payment of %.2f %s",
                            amount, currency);
                    Transaction transaction = new Transaction(timestamp, description,
                            account.getIban(), account.getIban(), splitAmount, "split");
                    transaction.setInvolvedAccounts(accountsForSplit);
                    transaction.setCurrency(currency);
                    account.getTransactions().add(transaction);
                } else {
                    String description = String.format("Split payment of %.2f %s",
                            amount, currency);
                    String error = "Account " + insufficientFundsAccount
                            + " has insufficient funds for a split payment.";
                    Transaction transaction = new Transaction(timestamp, description,
                            account.getIban(), account.getIban(), splitAmount, "splitFailed");
                    transaction.setInvolvedAccounts(accountsForSplit);
                    transaction.setCurrency(currency);
                    transaction.setError(error);
                    account.getTransactions().add(transaction);
                }
            }
        }
    }
}

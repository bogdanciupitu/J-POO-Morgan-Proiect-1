package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.classes.User;
import org.poo.classes.Alias;
import org.poo.classes.Exchange;
import org.poo.fileio.CommandInput;

import java.util.HashSet;

public final class SendMoneyCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String account = command.getAccount();
        double amount = command.getAmount();
        String receiver = command.getReceiver();
        int timestamp = command.getTimestamp();
        String description = command.getDescription();

        Account senderAccount = null;
        Account receiverAccount = null;

        for (User user : bank.getUsers()) {
            if (senderAccount == null) {
                senderAccount = bank.findAccount(user, account);
            }

            if (receiverAccount == null) {
                for (Alias alias : user.getAliases()) {
                    if (alias.getAlias().equals(receiver)) {
                        receiverAccount = bank.findAccount(user, alias.getAccount());
                        break;
                    }
                }

                if (receiverAccount == null) {
                    receiverAccount = bank.findAccount(user, receiver);
                }
            }

            if (senderAccount != null && receiverAccount != null) {
                break;
            }
        }

        if (senderAccount != null && receiverAccount != null) {
            if (senderAccount.getBalance() >= amount) {
                double conversionRate = bank.convert(senderAccount.getCurrency(),
                        receiverAccount.getCurrency(), new HashSet<>());
                double convertedAmount = amount * conversionRate;

                senderAccount.setBalance(senderAccount.getBalance() - amount);
                receiverAccount.setBalance(receiverAccount.getBalance() + convertedAmount);

                Transaction senderTransaction = new Transaction(timestamp, description,
                        senderAccount.getIban(), receiverAccount.getIban(), amount, "sent");
                senderAccount.getTransactions().add(senderTransaction);
                Transaction receiverTransaction = new Transaction(timestamp, description,
                        senderAccount.getIban(), receiverAccount.getIban(), convertedAmount,
                        "received");
                receiverAccount.getTransactions().add(receiverTransaction);

                for (Exchange exchange : bank.getExchanges()) {
                    if (exchange.getFrom().equals(senderAccount.getCurrency())) {
                        exchange.getTransactions().add(senderTransaction);
                    }
                }
            } else {
                Transaction transaction = new Transaction(timestamp, "Insufficient funds",
                        senderAccount.getIban(), receiverAccount.getIban(), amount, "sentFailed");
                senderAccount.getTransactions().add(transaction);
            }
        }
    }
}

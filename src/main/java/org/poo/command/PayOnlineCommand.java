package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Card;
import org.poo.classes.Transaction;
import org.poo.classes.User;
import org.poo.classes.Exchange;
import org.poo.classes.OneTimeCard;
import org.poo.utils.Utils;

import java.util.HashSet;

import static org.poo.classes.Bank.MIN_BALANCE_WARNING;

public final class PayOnlineCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();
        String description = command.getDescription();
        String commerciant = command.getCommerciant();
        String email = command.getEmail();

        User user = bank.findUser(email);
        boolean found = false;
        if (user != null) {
            for (Account account : user.getAccounts()) {
                if (account != null) {
                    for (Card card : account.getCards()) {
                        if (card.getCardNumber().equals(cardNumber)) {
                            found = true;

                            if (card.getStatus().equals("frozen")) {
                                Transaction transaction = new Transaction(timestamp,
                                        "The card is frozen", account.getIban(), commerciant,
                                        amount, "onlineFailed");
                                account.getTransactions().add(transaction);
                                return;
                            }

                            double conversionRate = bank.convert(currency, account.getCurrency(),
                                    new HashSet<>());
                            double convertedAmount = amount * conversionRate;

                            double remainingAmount = account.getBalance() - convertedAmount;
                            if (account.getBalance() < convertedAmount) {
                                Transaction transaction = new Transaction(timestamp,
                                        "Insufficient funds", account.getIban(), commerciant,
                                        amount, "onlineFailed");
                                account.getTransactions().add(transaction);
                                return;
                            }
                            if (remainingAmount < account.getMinBalance()) {
                                Transaction transaction = new Transaction(timestamp,
                                        "The card is frozen", account.getIban(), commerciant,
                                        amount, "onlineFailed");
                                account.getTransactions().add(transaction);
                                card.setStatus("frozen");
                                return;
                            }

                            if (account.getBalance() >= convertedAmount) {
                                account.setBalance(account.getBalance() - convertedAmount);
                                Transaction transaction = new Transaction(timestamp,
                                        "Card payment", account.getIban(), commerciant,
                                        convertedAmount, "online");
                                account.getTransactions().add(transaction);

                                if (card instanceof OneTimeCard) {
                                    Transaction deletionTransaction = new Transaction(timestamp,
                                            "The card has been destroyed", account.getIban(),
                                            card.getCardNumber(), 0, "cardDeleted");
                                    account.getTransactions().add(deletionTransaction);
                                    account.getCards().remove(card);
                                    String newCardNumber = Utils.generateCardNumber();
                                    account.getCards().add(new Card(newCardNumber, "active"));
                                    Transaction creationTransaction = new Transaction(timestamp,
                                            "New card created", account.getIban(), newCardNumber,
                                            0, "card");
                                    account.getTransactions().add(creationTransaction);
                                }
                                for (Exchange exchange : bank.getExchanges()) {
                                    if (exchange.getFrom().equals(currency)) {
                                        exchange.getTransactions().add(transaction);
                                    }
                                }
                            }

                            double balanceDifference = account.getBalance()
                                    - account.getMinBalance();
                            if (balanceDifference <= MIN_BALANCE_WARNING
                                    && balanceDifference > 0) {
                                Transaction transaction = new Transaction(timestamp,
                                        "You have reached the minimum amount of funds,"
                                                + " the card will be frozen", account.getIban(),
                                        account.getIban(), 0, "statusChange");
                                card.setStatus("frozen");
                                account.getTransactions().add(transaction);
                            } else if (balanceDifference < 0) {
                                card.setStatus("frozen");
                                Transaction transaction = new Transaction(timestamp,
                                        "The card is frozen", account.getIban(), account.getIban(),
                                        0, "statusChange");
                                account.getTransactions().add(transaction);
                            }
                            return;
                        }
                    }
                }
            }
        }

        if (!found) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Card not found");
            outputNode.put("timestamp", timestamp);
            result.set("output", outputNode);
            result.put("timestamp", timestamp);
            output.add(result);
        }
    }
}

package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Card;
import org.poo.classes.Transaction;
import org.poo.classes.User;
import org.poo.fileio.CommandInput;

import static org.poo.classes.Bank.MIN_BALANCE_WARNING;

public final class CheckCardStatusCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        boolean found = false;
        for (User user : bank.getUsers()) {
            for (Account account : user.getAccounts()) {
                if (account != null) {
                    for (Card card : account.getCards()) {
                        if (card.getCardNumber().equals(cardNumber)) {
                            found = true;
                            double balanceDifference = account.getBalance()
                                    - account.getMinBalance();

                            if (balanceDifference < 0) {
                                card.setStatus("frozen");
                                Transaction transaction = new Transaction(timestamp,
                                        "The card is frozen", account.getIban(), account.getIban(),
                                        0, "statusChange");
                                account.getTransactions().add(transaction);
                            } else if (balanceDifference <= MIN_BALANCE_WARNING) {
                                card.setStatus("warning");
                                Transaction transaction = new Transaction(timestamp,
                                        "You have reached the minimum amount of funds,"
                                                + " the card will be frozen", account.getIban(),
                                        account.getIban(), 0, "statusChange");
                                account.getTransactions().add(transaction);
                            }
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
            outputNode.put("timestamp", timestamp);
            outputNode.put("description", "Card not found");
            result.set("output", outputNode);
            result.put("timestamp", timestamp);
            output.add(result);
        }
    }
}

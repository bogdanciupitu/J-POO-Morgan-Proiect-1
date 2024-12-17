package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Card;
import org.poo.classes.Transaction;
import org.poo.classes.User;
import org.poo.fileio.CommandInput;

public final class DeleteCardCommand implements Command {
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
                            account.getCards().remove(card);

                            Transaction transaction = new Transaction(timestamp,
                                    "The card has been destroyed", account.getIban(),
                                    card.getCardNumber(), 0, "cardDeleted");
                            account.getTransactions().add(transaction);
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
    }
}

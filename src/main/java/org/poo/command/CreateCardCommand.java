package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Card;
import org.poo.classes.OneTimeCard;
import org.poo.classes.Transaction;
import org.poo.classes.User;
import org.poo.utils.Utils;

public final class CreateCardCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String account = command.getAccount();
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = bank.findUser(email);

        if (user != null) {
            Account userAccount = bank.findAccount(user, account);
            if (userAccount != null) {
                Card card = null;
                if (command.getCommand().equals("createCard")) {
                    card = new Card(Utils.generateCardNumber(), "active");
                } else if (command.getCommand().equals("createOneTimeCard")) {
                    card = new OneTimeCard(Utils.generateCardNumber(), "active");
                }
                userAccount.getCards().add(card);

                if (card != null) {
                    Transaction transaction = new Transaction(timestamp, "New card created",
                            account, card.getCardNumber(), 0, "card");
                    userAccount.getTransactions().add(transaction);
                }
            }
        }
    }
}

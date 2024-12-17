package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Card;
import org.poo.classes.User;
import org.poo.fileio.CommandInput;

public final class PrintUsersCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode usersArray = mapper.createArrayNode();

        for (User user : bank.getUsers()) {
            ObjectNode userNode = mapper.createObjectNode();
            userNode.put("firstName", user.getFirstName());
            userNode.put("lastName", user.getLastName());
            userNode.put("email", user.getEmail());

            ArrayNode accounts = mapper.createArrayNode();
            for (Account account : user.getAccounts()) {
                if (account != null) {
                    ObjectNode accountNode = mapper.createObjectNode();
                    accountNode.put("IBAN", account.getIban());
                    accountNode.put("balance", account.getBalance());
                    accountNode.put("currency", account.getCurrency());
                    accountNode.put("type", account.getAccountType());

                    ArrayNode cards = mapper.createArrayNode();
                    for (Card card : account.getCards()) {
                        ObjectNode cardNode = mapper.createObjectNode();
                        cardNode.put("cardNumber", card.getCardNumber());
                        cardNode.put("status", card.getStatus());
                        cards.add(cardNode);
                    }
                    accountNode.set("cards", cards);
                    accounts.add(accountNode);
                }
            }
            userNode.set("accounts", accounts);
            usersArray.add(userNode);
        }

        ObjectNode result = mapper.createObjectNode();
        result.put("command", command.getCommand());
        result.set("output", usersArray);
        result.put("timestamp", command.getTimestamp());
        output.add(result);
    }
}

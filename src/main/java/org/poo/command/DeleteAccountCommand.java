package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.classes.User;

public final class DeleteAccountCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String account = command.getAccount();
        int timestamp = command.getTimestamp();
        String email = command.getEmail();

        User user = bank.findUser(email);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        result.put("command", command.getCommand());
        result.put("timestamp", timestamp);

        if (user != null) {
            Account userAccount = bank.findAccount(user, account);
            if (userAccount != null) {
                if (userAccount.getBalance() == 0) {
                    userAccount.getCards().clear();
                    user.getAccounts().remove(userAccount);

                    Transaction transaction = new Transaction(timestamp,
                            "The card has been destroyed", userAccount.getIban(),
                            userAccount.getIban(), 0, "accountDeleted");
                    userAccount.getTransactions().add(transaction);

                    ObjectNode outputNode = mapper.createObjectNode();
                    outputNode.put("success", "Account deleted");
                    outputNode.put("timestamp", timestamp);
                    result.set("output", outputNode);
                } else {
                    Transaction transaction = new Transaction(timestamp,
                            "Account couldn't be deleted - there are funds remaining",
                            userAccount.getIban(), userAccount.getIban(), 0, "accountDeleted");
                    userAccount.getTransactions().add(transaction);

                    ObjectNode outputNode = mapper.createObjectNode();
                    outputNode.put("error",
                            "Account couldn't be deleted - see org.poo.transactions for details");
                    outputNode.put("timestamp", timestamp);
                    result.set("output", outputNode);
                }
            }
        }
        output.add(result);
    }
}

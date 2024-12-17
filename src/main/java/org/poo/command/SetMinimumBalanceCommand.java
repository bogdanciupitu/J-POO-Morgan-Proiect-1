package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.User;
import org.poo.fileio.CommandInput;

public final class SetMinimumBalanceCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        double amount = command.getAmount();
        String account = command.getAccount();
        int timestamp = command.getTimestamp();

        boolean isFound = false;

        for (User user : bank.getUsers()) {
            Account userAccount = bank.findAccount(user, account);
            if (userAccount != null) {
                if (user.getAccounts().contains(userAccount)) {
                    userAccount.setMinBalance(amount);
                    isFound = true;
                    break;
                }
            }
        }

        if (!isFound) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", timestamp);
            result.set("output", outputNode);
            result.put("timestamp", timestamp);
        }
    }
}

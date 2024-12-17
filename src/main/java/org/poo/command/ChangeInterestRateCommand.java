package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.classes.Account;
import org.poo.classes.SavingsAccount;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.classes.User;
import org.poo.fileio.CommandInput;

public final class ChangeInterestRateCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        int timestamp = command.getTimestamp();
        String accountIBAN = command.getAccount();
        double newInterestRate = command.getInterestRate();

        for (User user : bank.getUsers()) {
            for (Account account : user.getAccounts()) {
                if (account != null) {
                    if (account.getIban().equals(accountIBAN)) {
                        if (account instanceof SavingsAccount savingsAccount) {
                            savingsAccount.setInterestRate(newInterestRate);

                            Transaction transaction = new Transaction(timestamp,
                                    "Interest rate of the account changed to " + newInterestRate,
                                    account.getIban(), account.getIban(), 0, "interest");
                            savingsAccount.getTransactions().add(transaction);
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode result = mapper.createObjectNode();
                            result.put("command", command.getCommand());
                            ObjectNode outputNode = mapper.createObjectNode();
                            outputNode.put("description", "This is not a savings account");
                            outputNode.put("timestamp", timestamp);
                            result.set("output", outputNode);
                            result.put("timestamp", timestamp);
                            output.add(result);
                        }
                    }
                }
            }
        }
    }
}

package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.classes.SavingsAccount;
import org.poo.fileio.CommandInput;

public final class AddInterestCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String account = command.getAccount();
        int timestamp = command.getTimestamp();

        Account accountIBAN = bank.findAccountByIBAN(account);
        if (accountIBAN != null) {
            if (accountIBAN instanceof SavingsAccount savingsAccount) {
                double interestRate = savingsAccount.getBalance()
                        * savingsAccount.getInterestRate();
                savingsAccount.setBalance(savingsAccount.getBalance() + interestRate);

                Transaction transaction = new Transaction(timestamp, "Interest added",
                        accountIBAN.getIban(), accountIBAN.getIban(), interestRate, "addInterest");
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

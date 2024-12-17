package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.User;
import org.poo.fileio.CommandInput;

public final class AddFundsCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String account = command.getAccount();
        double amount = command.getAmount();
        int timestamp = command.getTimestamp();

        for (User user : bank.getUsers()) {
            Account userAccount = bank.findAccount(user, account);
            if (userAccount != null) {
                userAccount.setBalance(userAccount.getBalance() + amount);
            }
        }
    }
}

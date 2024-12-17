package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.classes.Account;
import org.poo.classes.AccountFactory;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.classes.User;
import org.poo.fileio.CommandInput;
import org.poo.utils.Utils;

public final class AddAccountCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String email = command.getEmail();
        String currency = command.getCurrency();
        String accountType = command.getAccountType();
        int timestamp = command.getTimestamp();
        double interestRate = command.getInterestRate();

        User user = bank.findUser(email);
        if (user != null) {
            String iban = Utils.generateIBAN();

            Account account = AccountFactory.createAccount(currency, accountType, timestamp, iban,
                    interestRate);
            user.getAccounts().add(account);

            Transaction transaction = new Transaction(timestamp, "New account created", iban, iban,
                    0, "account");
            if (account != null) {
                account.getTransactions().add(transaction);
            }
        }
    }
}

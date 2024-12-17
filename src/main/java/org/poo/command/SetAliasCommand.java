package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.classes.Bank;
import org.poo.classes.User;
import org.poo.classes.Alias;
import org.poo.fileio.CommandInput;

public final class SetAliasCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String email = command.getEmail();
        String alias = command.getAlias();
        String account = command.getAccount();

        User user = bank.findUser(email);
        if (user != null) {
            user.getAliases().add(new Alias(alias, account));
        }
    }
}

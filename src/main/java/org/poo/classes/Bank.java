package org.poo.classes;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.UserInput;
import org.poo.utils.Utils;
import org.poo.command.AddAccountCommand;
import org.poo.command.AddFundsCommand;
import org.poo.command.AddInterestCommand;
import org.poo.command.ChangeInterestRateCommand;
import org.poo.command.CheckCardStatusCommand;
import org.poo.command.CreateCardCommand;
import org.poo.command.DeleteAccountCommand;
import org.poo.command.DeleteCardCommand;
import org.poo.command.PayOnlineCommand;
import org.poo.command.PrintTransactionsCommand;
import org.poo.command.PrintUsersCommand;
import org.poo.command.ReportCommand;
import org.poo.command.SendMoneyCommand;
import org.poo.command.SetAliasCommand;
import org.poo.command.SetMinimumBalanceCommand;
import org.poo.command.SplitPaymentCommand;
import org.poo.command.SpendingsReportCommand;

import java.util.ArrayList;
import java.util.Set;

@Getter
@Setter
public final class Bank {
    private ArrayList<User> users;
    private ArrayList<Exchange> exchanges;
    public static final double MIN_BALANCE_WARNING = 30.0;

    private static Bank instance;

    private Bank() {
        this.users = new ArrayList<>();
        this.exchanges = new ArrayList<>();
    }

    /**
     *  Singleton pattern
     *  The instance of the Bank class is created only once
     *
     * @return the single instance of the Bank class
     */
    public static Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }

    /**
     * Initializes the bank with the given users and exchanges
     * @param inputUsers the array of users
     * @param inputExchanges the array of exchanges
     */
    public void initialize(final UserInput[] inputUsers, final ExchangeInput[] inputExchanges) {
        this.users = new ArrayList<>();
        for (UserInput inputUser : inputUsers) {
            this.users.add(new User(inputUser.getFirstName(), inputUser.getLastName(),
                    inputUser.getEmail()));
        }

        this.exchanges = new ArrayList<Exchange>();
        for (ExchangeInput inputExchange : inputExchanges) {
            this.exchanges.add(new Exchange(inputExchange));
        }
    }

    /**
     * Processes the given array of commands and generates the output due to the methods
     * of the commands
     *
     * @param commands the array of commands
     * @param output the array of output
     */
    public void processTransactions(final CommandInput[] commands, final ArrayNode output) {
        Utils.resetRandom();
        for (CommandInput command : commands) {
            switch (command.getCommand()) {
                case "addAccount":
                    new AddAccountCommand().execute(command, output);
                    break;
                case "createCard", "createOneTimeCard":
                    new CreateCardCommand().execute(command, output);
                    break;
                case "printUsers":
                    new PrintUsersCommand().execute(command, output);
                    break;
                case "addFunds":
                    new AddFundsCommand().execute(command, output);
                    break;
                case "deleteAccount":
                    new DeleteAccountCommand().execute(command, output);
                    break;
                case "deleteCard":
                    new DeleteCardCommand().execute(command, output);
                    break;
                case "setMinimumBalance":
                    new SetMinimumBalanceCommand().execute(command, output);
                    break;
                case "payOnline":
                    new PayOnlineCommand().execute(command, output);
                    break;
                case "sendMoney":
                     new SendMoneyCommand().execute(command, output);
                    break;
                case "setAlias":
                    new SetAliasCommand().execute(command, output);
                    break;
                case "printTransactions":
                    new PrintTransactionsCommand().execute(command, output);
                    break;
                case "checkCardStatus":
                    new CheckCardStatusCommand().execute(command, output);
                    break;
                case "changeInterestRate":
                    new ChangeInterestRateCommand().execute(command, output);
                    break;
                case "splitPayment":
                    new SplitPaymentCommand().execute(command, output);
                    break;
                case "report":
                    new ReportCommand().execute(command, output);
                    break;
                case "spendingsReport":
                    new SpendingsReportCommand().execute(command, output);
                    break;
                case "addInterest":
                    new AddInterestCommand().execute(command, output);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Finds the user by email
     *
     * @param email the email of the user
     * @return the user with the given email, or null if the user is not found
     */
    public User findUser(final String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }

        return null;
    }

    /**
     * Finds user by their account
     *
     * @param account the account of the user
     * @return the user with the given account, or null if the user is not found
     */
    public User findUserByAccount(final String account) {
        for (User user : users) {
            for (Account userAccount : user.getAccounts()) {
                if (userAccount != null && userAccount.getIban().equals(account)) {
                    return user;
                }
            }
        }

        return null;
    }

    /**
     *  Finds the account of the user
     *
     * @param user the user whose account is to be found
     * @param account the account to be found
     * @return the account of the user, or null if the account is not found
     */
    public Account findAccount(final User user, final String account) {
        for (Account userAccount : user.getAccounts()) {
            if (userAccount != null && userAccount.getIban().equals(account)) {
                return userAccount;
            }
        }

        return null;
    }

    /**
     * Finds the account by IBAN
     *
     * @param accountIBAN the IBAN of the account
     * @return the account with the given IBAN, or null if the account is not found
     */
    public Account findAccountByIBAN(final String accountIBAN) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account != null && account.getIban().equals(accountIBAN)) {
                    return account;
                }
            }
        }

        return null;
    }

    /**
     * Converts the currency from one to another using  exchange rates
     *
     * @param from the currency to convert from
     * @param to the currency to convert to
     * @param visited a set of visited currencies to avoid infinite loops
     * @return the conversion rate from one currency to another
     */
    public double convert(final String from, final String to, final Set<String> visited) {
        if (from.equals(to)) {
            return 1.0;
        }

        visited.add(from);

        for (Exchange exchange : exchanges) {
            if (exchange.getFrom().equals(from) && !visited.contains(exchange.getTo())) {
                double next = convert(exchange.getTo(), to, visited);
                if (next != 0) {
                    return exchange.getRate() * next;
                }
            } else if (exchange.getTo().equals(from) && !visited.contains(exchange.getFrom())) {
                double next = convert(exchange.getFrom(), to, visited);
                if (next != 0) {
                    return next / exchange.getRate();
                }
            }
        }

        return 0;
    }
}

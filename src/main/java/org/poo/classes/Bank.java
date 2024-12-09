package org.poo.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.UserInput;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Bank {
    private ArrayList<User> users;
    private ArrayList<Exchange> exchanges;

    public Bank(final UserInput[] inputUsers, final ExchangeInput[] inputExchanges) {
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

    public void processTransactions(final CommandInput[] commands, final ArrayNode output) {
        Utils.resetRandom();
        for (CommandInput command : commands) {
            switch (command.getCommand()) {
                case "addAccount":
                    addAccount(command);
                    break;
                case "createCard":
                    createCard(command);
                    break;
                case "createOneTimeCard":
                    // createOneTimeCard(command);
                    createCard(command);
                    break;
                case "printUsers":
                    printUsers(command, output);
                    break;
                case "addFunds":
                    addFunds(command);
                    break;
                case "deleteAccount":
                     deleteAccount(command, output);
                    break;
                case "deleteCard":
                     deleteCard(command);
                    break;
                case "setMinimumBalance":
                     setMinimumBalance(command);
                    break;
                case "payOnline":
                     payOnline(command, output);
                    break;
                case "sendMoney":
                     sendMoney(command);
                    break;
                case "setAlias":
                     setAlias(command);
                    break;
                case "printTransactions":
                     printTransactions(command, output);
                    break;
                case "checkCardStatus":
                     checkCardStatus(command, output);
                    break;
                case "changeInterestRate":
                     changeInterestRate(command);
                    break;
                case "splitPayment":
                     splitPayment(command);
                    break;
                case "report":
                     report(command, output);
                    break;
                case "spendingsReport":
                     spendingsReport(command, output);
                    break;
                case "addInterest":
                     addInterest(command);
                    break;
                default:
                    break;
            }
        }
    }

    private void addAccount(final CommandInput command) {
        String email = command.getEmail();
        String currency = command.getCurrency();
        String accountType = command.getAccountType();
        int timestamp = command.getTimestamp();
        double interestRate = command.getInterestRate();

        User user = findUser(email);
        if (user != null) {
            String iban = Utils.generateIBAN();
            Account account = null;
            if (accountType.equals("classic")) {
                account = new ClassicAccount(currency, accountType, timestamp, 0, iban,
                        new ArrayList<>());
            } else if (accountType.equals("savings")) {
                account = new SavingsAccount(currency, accountType, timestamp, 0, iban,
                        interestRate, new ArrayList<>());
            }

            // DACA E SA FOLOSESC FACTORY
//            Account account = AccountFactory.createAccount(currency, accountType, timestamp, iban,
//                    interestRate);
            user.getAccounts().add(account);

            Transaction transaction = new Transaction(timestamp, "New account created", iban, iban,
                    0, "account");
            if (account != null) {
                account.getTransactions().add(transaction);
            }
        }
    }

    private void createCard(final CommandInput command) {
        String account = command.getAccount();
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = findUser(email);

        if (user != null) {
            Account userAccount = findAccount(user, account);
            if (userAccount != null) {
                Card card = null;
                if (command.getCommand().equals("createCard")) {
                    card = new Card(Utils.generateCardNumber(), "active");
                } else if (command.getCommand().equals("createOneTimeCard")) {
                    card = new OneTimeCard(Utils.generateCardNumber(), "active");
                }
                userAccount.getCards().add(card);

                Transaction transaction = new Transaction(timestamp, "New card created", account,
                        card.getCardNumber(), 0, "card");
                userAccount.getTransactions().add(transaction);
            }
        }
    }

    private User findUser(final String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private Account findAccount(final User user, final String account) {
        for (Account userAccount : user.getAccounts()) {
            if (userAccount != null && userAccount.getIban().equals(account)) {
                return userAccount;
            }
        }
        return null;
    }

    private void printUsers(final CommandInput command, final ArrayNode output) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode usersArray = mapper.createArrayNode();

        for (User user : users) {
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

    private void addFunds(final CommandInput command) {
        String account = command.getAccount();
        double amount = command.getAmount();
        int timestamp = command.getTimestamp();

        for (User user : users) {
            Account userAccount = findAccount(user, account);
            if (userAccount != null) {
                userAccount.setBalance(userAccount.getBalance() + amount);
            }
        }
    }

    private void deleteAccount(final CommandInput command, final ArrayNode output) {
        String account = command.getAccount();
        int timestamp = command.getTimestamp();
        String email = command.getEmail();

        User user = findUser(email);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        result.put("command", command.getCommand());
        result.put("timestamp", timestamp);

        if (user != null) {
            Account userAccount = findAccount(user, account);
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

    private void deleteCard(final CommandInput command) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        boolean found = false;
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account != null) {
                    for (Card card : account.getCards()) {
                        if (card.getCardNumber().equals(cardNumber)) {
                            found = true;
                            account.getCards().remove(card);

                            Transaction transaction = new Transaction(timestamp,
                                    "The card has been destroyed", account.getIban(),
                                    card.getCardNumber(), 0, "cardDeleted");
                            account.getTransactions().add(transaction);
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
    }

    private void setMinimumBalance(final CommandInput command) {
        double amount = command.getAmount();
        String account = command.getAccount();
        int timestamp = command.getTimestamp();

        for (User user : users) {
            Account userAccount = findAccount(user, account);
            if (userAccount != null) {
                userAccount.setMinBalance(amount);
            }
        }
    }

    private void payOnline(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();
        String description = command.getDescription();
        String commerciant = command.getCommerciant();
        String email = command.getEmail();

        User user = findUser(email);
        boolean found = false;
        if (user != null) {
            for (Account account : user.getAccounts()) {
                if (account != null) {
                    for (Card card : account.getCards()) {
                        if (card.getCardNumber().equals(cardNumber)) {
                            found = true;
                            double convertedAmount = convertCurrency(amount, currency,
                                    account.getCurrency());
                            if (account.getBalance() >= amount) {
                                account.setBalance(account.getBalance() - amount);
                                Transaction transaction = new Transaction(timestamp,
                                        "Card payment", account.getIban(), commerciant, amount,
                                        "online");
                                account.getTransactions().add(transaction);
                                for (Exchange exchange : exchanges) {
                                    if (exchange.getFrom().equals(currency)) {
                                        exchange.getTransactions().add(transaction);
                                    }
                                }
                            } else {
                                Transaction transaction = new Transaction(timestamp,
                                        "Insufficient funds", account.getIban(), commerciant,
                                        amount, "onlineFailed");
                                account.getTransactions().add(transaction);
                            }
                        }
                    }
                }
            }
        }
        if (!found) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Card not found");
            outputNode.put("timestamp", timestamp);
            result.set("output", outputNode);
            result.put("timestamp", timestamp);
            output.add(result);
        }
    }

    private void sendMoney(final CommandInput command) {
        String account = command.getAccount();
        double amount = command.getAmount();
        String receiver = command.getReceiver();
        int timestamp = command.getTimestamp();
        String description = command.getDescription();

        Account senderAccount = null;
        Account receiverAccount = null;

        for (User user : users) {
            if (senderAccount == null) {
                senderAccount = findAccount(user, account);
            }
            if (receiverAccount == null) {
                for (Alias alias : user.getAliases()) {
                    if (alias.getAlias().equals(receiver)) {
                        receiverAccount = findAccount(user, alias.getAccount());
                        break;
                    }
                }
                if (receiverAccount == null) {
                    receiverAccount = findAccount(user, receiver);
                }
            }
            if (senderAccount != null && receiverAccount != null) {
                break;
            }
        }

        if (senderAccount != null && receiverAccount != null) {
            if (senderAccount.getBalance() >= amount) {
                double convertedAmount = convertCurrency(amount, senderAccount.getCurrency(),
                        receiverAccount.getCurrency());
                senderAccount.setBalance(senderAccount.getBalance() - amount);
                receiverAccount.setBalance(receiverAccount.getBalance() + convertedAmount);
                Transaction transaction = new Transaction(timestamp, description,
                        senderAccount.getIban(), receiverAccount.getIban(), amount, "sent");
                senderAccount.getTransactions().add(transaction);
                receiverAccount.getTransactions().add(transaction);
                for (Exchange exchange : exchanges) {
                    if (exchange.getFrom().equals(senderAccount.getCurrency())) {
                        exchange.getTransactions().add(transaction);
                    }
                }
            }
        }
    }

    private void setAlias(final CommandInput command) {
        String email = command.getEmail();
        String alias = command.getAlias();
        String account = command.getAccount();

        User user = findUser(email);
        if (user != null) {
            user.getAliases().add(new Alias(alias, account));
        }
    }

    private void printTransactions(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        int commandTimestamp = command.getTimestamp();

        User user = findUser(email);
        if (user != null) {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode transactionsArray = mapper.createArrayNode();

            for (Account account : user.getAccounts()) {
                if (account != null) {
                    for (Transaction transaction : account.getTransactions()) {
                        ObjectNode transactionNode = mapper.createObjectNode();
                        if (transaction.getTransferType().equals("account")) {
                            transactionNode.put("description", transaction.getDescription());
                            transactionNode.put("timestamp", transaction.getTimestamp());
                            transactionsArray.add(transactionNode);
                        } else if (transaction.getTransferType().equals("accountDeleted")) {
                            transactionNode.put("account", account.getIban());
                            transactionNode.put("card", transaction.getReceiverIBAN());
                            transactionNode.put("description", transaction.getDescription());
                            transactionNode.put("timestamp", transaction.getTimestamp());
                            transactionsArray.add(transactionNode);
                        } else if (transaction.getTransferType().equals("card")) {
                            transactionNode.put("account", account.getIban());
                            transactionNode.put("card", transaction.getReceiverIBAN());
                            transactionNode.put("cardHolder", user.getEmail());
                            transactionNode.put("description", transaction.getDescription());
                            transactionNode.put("timestamp", transaction.getTimestamp());
                            transactionsArray.add(transactionNode);
                        } else if (transaction.getTransferType().equals("sent")) {
                            transactionNode.put("timestamp", transaction.getTimestamp());
                            transactionNode.put("description", transaction.getDescription());
                            transactionNode.put("senderIBAN", transaction.getSenderIBAN());
                            transactionNode.put("receiverIBAN", transaction.getReceiverIBAN());
                            transactionNode.put("amount", transaction.getAmount() + " "
                                    + account.getCurrency());
                            transactionNode.put("transferType", transaction.getTransferType());
                            transactionsArray.add(transactionNode);
                        } else if (transaction.getTransferType().equals("online")) {
                            transactionNode.put("amount", transaction.getAmount());
                            transactionNode.put("commerciant", transaction.getReceiverIBAN());
                            transactionNode.put("description", transaction.getDescription());
                            transactionNode.put("timestamp", transaction.getTimestamp());
                            transactionsArray.add(transactionNode);
                        } else if (transaction.getTransferType().equals("onlineFailed")) {
                            transactionNode.put("description", transaction.getDescription());
                            transactionNode.put("timestamp", transaction.getTimestamp());
                            transactionsArray.add(transactionNode);
                        } else if (transaction.getTransferType().equals("split")) {
                            transactionNode.put("amount", transaction.getAmount());
                            transactionNode.put("currency", account.getCurrency());
                            double totalAmount = transaction.getAmount() * account.getTransactions().size();
                            transactionNode.put("description", transaction.getDescription()
                                    + totalAmount + " " + account.getCurrency());
                            ArrayNode involvedAccounts = mapper.createArrayNode();
                            for (Account acc : user.getAccounts()) {
                                involvedAccounts.add(acc.getIban());
                            }
                            transactionNode.set("involvedAccounts", involvedAccounts);
                            transactionNode.put("timestamp", transaction.getTimestamp());
                            transactionsArray.add(transactionNode);
                        } else if (transaction.getTransferType().equals("statusChange")) {
                            transactionNode.put("description", transaction.getDescription());
                            transactionNode.put("timestamp", transaction.getTimestamp());
                            transactionsArray.add(transactionNode);
                        }
                    }
                }
            }

            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            result.set("output", transactionsArray);
            result.put("timestamp", commandTimestamp);
            output.add(result);
        }
    }

    private void checkCardStatus(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        boolean found = false;
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account != null) {
                    for (Card card : account.getCards()) {
                        if (card.getCardNumber().equals(cardNumber)) {
                            found = true;
                            double balanceDifference = account.getBalance()
                                    - account.getMinBalance();
                            if (balanceDifference < 0) {
                                card.setStatus("frozen");
                                Transaction transaction = new Transaction(timestamp,
                                        "The card is frozen", account.getIban(), account.getIban(),
                                        0, "statusChange");
                                account.getTransactions().add(transaction);
                            } else if (balanceDifference <= 30) {
                                card.setStatus("warning");
                                Transaction transaction = new Transaction(timestamp,
                                        "You have reached the minimum amount of funds," +
                                                " the card will be frozen", account.getIban(),
                                        account.getIban(), 0, "statusChange");
                            } else {
                                card.setStatus("active");
                            }
                        }
                    }
                }
            }
        }
        if (!found) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("timestamp", timestamp);
            outputNode.put("description", "Card not found");
            result.set("output", output);
            result.put("timestamp", timestamp);
        }
    }

    private void changeInterestRate(final CommandInput command) {
        int timestamp = command.getTimestamp();
        String accountIBAN = command.getAccount();
        double newInterestRate = command.getInterestRate();

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account != null) {
                    if (account.getIban().equals(accountIBAN)
                            && account instanceof SavingsAccount savingsAccount) {
                        savingsAccount.setInterestRate(newInterestRate);

                        Transaction transaction = new Transaction(timestamp,
                                "Interest rate changed", account.getIban(), account.getIban(),
                                0, "interest");
                        savingsAccount.getTransactions().add(transaction);
                    }
                }
            }
        }
    }

    private void splitPayment(final CommandInput command) {
        List<String> accountsForSplit = command.getAccounts();
        int timestamp = command.getTimestamp();
        String currency = command.getCurrency();
        double amount = command.getAmount();

        double splitAmount = amount / accountsForSplit.size();
        boolean canSplit = true;

        for (String accountIBAN : accountsForSplit) {
            Account account = findAccountByIBAN(accountIBAN);
            if (account == null || account.getBalance() < splitAmount) {
                canSplit = false;
                break;
            }
        }

        for (String accountIBAN : accountsForSplit) {
            Account account = findAccountByIBAN(accountIBAN);
            if (account != null) {
                if (canSplit) {
                    double convertedAmount = convertCurrency(splitAmount, currency, account.getCurrency());
                    account.setBalance(account.getBalance() - convertedAmount);
                    Transaction transaction = new Transaction(timestamp, "Split payment of ",
                            account.getIban(), account.getIban(), splitAmount, "split");
                    account.getTransactions().add(transaction);
                } else {
                    Transaction transaction = new Transaction(timestamp, "Insufficient funds",
                            account.getIban(), account.getIban(), splitAmount, "splitFailed");
                    account.getTransactions().add(transaction);
                }
            }
        }
    }

    private Account findAccountByIBAN(final String accountIBAN) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account != null && account.getIban().equals(accountIBAN)) {
                    return account;
                }
            }
        }
        return null;
    }

    private double convertCurrency(final double amount, final String from, final String to) {
        if (from.equals(to)) {
            return amount;
        }

        for (Exchange exchange : exchanges) {
            if (exchange.getFrom().equals(from) && exchange.getTo().equals(to)) {
                return amount * exchange.getRate();
            }
        }
        return amount;
    }

    private void report(final CommandInput command, final ArrayNode output) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String account = command.getAccount();

        Account accountIBAN = findAccountByIBAN(account);
        if (accountIBAN != null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode report = mapper.createObjectNode();
            report.put("IBAN", accountIBAN.getIban());
            report.put("balance", accountIBAN.getBalance());
            report.put("currency", accountIBAN.getCurrency());

            ArrayNode transactions = mapper.createArrayNode();
            for (Transaction transaction : accountIBAN.getTransactions()) {
                if (transaction.getTimestamp() >= startTimestamp
                        && transaction.getTimestamp() <= endTimestamp) {
                    // logica
                }
            }

            report.set("transactions", transactions);
            output.add(report);
        }
    }

    private void spendingsReport(final CommandInput command, final ArrayNode output) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String account = command.getAccount();

        Account accountIBAN = findAccountByIBAN(account);
        if (accountIBAN != null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode report = mapper.createObjectNode();
            report.put("IBAN", accountIBAN.getIban());
            report.put("balance", accountIBAN.getBalance());
            report.put("currency", accountIBAN.getCurrency());

//            ObjectNode spendings = mapper.createObjectNode();
//            for (Commerciants category)
        }
    }

    private void addInterest(final CommandInput command) {
        String account = command.getAccount();
        int timestamp = command.getTimestamp();

        Account accountIBAN = findAccountByIBAN(account);
        if (accountIBAN != null) {
            if (accountIBAN instanceof SavingsAccount savingsAccount) {
                double interestRate = savingsAccount.getBalance() * savingsAccount.getInterestRate();
                savingsAccount.setBalance(savingsAccount.getBalance() + interestRate);

                Transaction transaction = new Transaction(timestamp, "Interest added", accountIBAN.getIban(),
                        accountIBAN.getIban(), interestRate, "addInterest");
                savingsAccount.getTransactions().add(transaction);
            }
        }
    }
}

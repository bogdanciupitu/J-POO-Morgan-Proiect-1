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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

@Getter
@Setter
public final class Bank {
    private ArrayList<User> users;
    private ArrayList<Exchange> exchanges;
    private static final double MIN_BALANCE_WARNING = 30.0;

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
//            this.exchanges.add(new Exchange(inputExchange));
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
                    addAccount(command);
                    break;
                case "createCard":
                    createCard(command);
                    break;
                case "createOneTimeCard":
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
                     changeInterestRate(command, output);
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
                     addInterest(command, output);
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

            // factory pattern
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

    private User findUserByAccount(final String account) {
        for (User user : users) {
            for (Account userAccount : user.getAccounts()) {
                if (userAccount != null && userAccount.getIban().equals(account)) {
                    return user;
                }
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

        boolean isFound = false;

        for (User user : users) {
            Account userAccount = findAccount(user, account);
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

                            if (card.getStatus().equals("frozen")) {
                                Transaction transaction = new Transaction(timestamp,
                                        "The card is frozen", account.getIban(), commerciant,
                                        amount, "onlineFailed");
                                account.getTransactions().add(transaction);
                                return;
                            }

                            double conversionRate = convert(currency, account.getCurrency(),
                                    new HashSet<>());
                            double convertedAmount = amount * conversionRate;

//                            double remainingAmount = account.getBalance() - convertedAmount;
//                            if (remainingAmount < account.getMinBalance()) {
//                                Transaction transaction = new Transaction(timestamp,
//                                        "The card is frozen", account.getIban(), commerciant,
//                                        amount, "onlineFailed");
//                                account.getTransactions().add(transaction);
//                                card.setStatus("frozen");
//                                return;
//                            }

                            if (account.getBalance() >= convertedAmount) {
                                account.setBalance(account.getBalance() - convertedAmount);
                                Transaction transaction = new Transaction(timestamp,
                                        "Card payment", account.getIban(), commerciant,
                                        convertedAmount, "online");
                                account.getTransactions().add(transaction);

                                if (card instanceof OneTimeCard) {
                                    Transaction deletionTransaction = new Transaction(timestamp,
                                            "The card has been destroyed", account.getIban(),
                                            card.getCardNumber(), 0, "cardDeleted");
                                    account.getTransactions().add(deletionTransaction);
                                    account.getCards().remove(card);
                                    String newCardNumber = Utils.generateCardNumber();
                                    account.getCards().add(new Card(newCardNumber, "active"));
                                    Transaction creationTransaction = new Transaction(timestamp,
                                            "New card created", account.getIban(), newCardNumber,
                                            0, "card");
                                    account.getTransactions().add(creationTransaction);
                                }
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

                            double balanceDifference = account.getBalance()
                                    - account.getMinBalance();
                            if (balanceDifference <= MIN_BALANCE_WARNING
                                    && balanceDifference > 0) {
                                Transaction transaction = new Transaction(timestamp,
                                        "You have reached the minimum amount of funds,"
                                                + " the card will be frozen", account.getIban(),
                                        account.getIban(), 0, "statusChange");
                                card.setStatus("frozen");
                                account.getTransactions().add(transaction);
                            } else if (balanceDifference < 0) {
                                card.setStatus("frozen");
                                Transaction transaction = new Transaction(timestamp,
                                        "The card is frozen", account.getIban(), account.getIban(),
                                        0, "statusChange");
                                account.getTransactions().add(transaction);
                            }
                            return;
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
                double conversionRate = convert(senderAccount.getCurrency(),
                        receiverAccount.getCurrency(), new HashSet<>());
                double convertedAmount = amount * conversionRate;
                senderAccount.setBalance(senderAccount.getBalance() - amount);
                receiverAccount.setBalance(receiverAccount.getBalance() + convertedAmount);
                Transaction senderTransaction = new Transaction(timestamp, description,
                        senderAccount.getIban(), receiverAccount.getIban(), amount, "sent");
                senderAccount.getTransactions().add(senderTransaction);
                Transaction receiverTransaction = new Transaction(timestamp, description,
                        senderAccount.getIban(), receiverAccount.getIban(), convertedAmount,
                        "received");
                receiverAccount.getTransactions().add(receiverTransaction);
                for (Exchange exchange : exchanges) {
                    if (exchange.getFrom().equals(senderAccount.getCurrency())) {
                        exchange.getTransactions().add(senderTransaction);
                    }
                }
            } else {
                Transaction transaction = new Transaction(timestamp, "Insufficient funds",
                        senderAccount.getIban(), receiverAccount.getIban(), amount, "sentFailed");
                senderAccount.getTransactions().add(transaction);
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

            List<Transaction> allTransactions = new ArrayList<>();

            for (Account account : user.getAccounts()) {
                if (account != null) {
                    allTransactions.addAll(account.getTransactions());
                }
            }

            allTransactions.sort(Comparator.comparingInt(Transaction::getTimestamp));

            for (Transaction transaction : allTransactions) {
                ObjectNode transactionNode = mapper.createObjectNode();
                if (transaction.getTransferType().equals("account")) {
                    transactionNode.put("description", transaction.getDescription());
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionsArray.add(transactionNode);
                } else if (transaction.getTransferType().equals("accountDeleted")) {
                    transactionNode.put("description", transaction.getDescription());
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionsArray.add(transactionNode);
                } else if (transaction.getTransferType().equals("card")) {
                    transactionNode.put("account", transaction.getSenderIBAN());
                    transactionNode.put("card", transaction.getReceiverIBAN());
                    transactionNode.put("cardHolder", user.getEmail());
                    transactionNode.put("description", transaction.getDescription());
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionsArray.add(transactionNode);
                } else if (transaction.getTransferType().equals("cardDeleted")) {
                    transactionNode.put("account", transaction.getSenderIBAN());
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
                            + findAccountByIBAN(transaction.getSenderIBAN()).getCurrency());
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
                    transactionNode.put("currency", transaction.getCurrency());
                    double totalAmount = transaction.getAmount()
                            * findAccountByIBAN(transaction.getInvolvedAccounts().getFirst())
                            .getTransactions().size();
                    transactionNode.put("description", transaction.getDescription());
                    ArrayNode involvedAccounts = mapper.createArrayNode();
                    for (String involvedAccount : transaction.getInvolvedAccounts()) {
                        involvedAccounts.add(involvedAccount);
                    }
                    transactionNode.set("involvedAccounts", involvedAccounts);
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionsArray.add(transactionNode);
                } else if (transaction.getTransferType().equals("statusChange")) {
                    transactionNode.put("description", transaction.getDescription());
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionsArray.add(transactionNode);
                } else if (transaction.getTransferType().equals("sentFailed")) {
                    transactionNode.put("description", transaction.getDescription());
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionsArray.add(transactionNode);
                } else if (transaction.getTransferType().equals("received")) {
                    transactionNode.put("amount", transaction.getAmount() + " "
                            + findAccountByIBAN(transaction.getReceiverIBAN()).getCurrency());
                    transactionNode.put("description", transaction.getDescription());
                    transactionNode.put("receiverIBAN", transaction.getReceiverIBAN());
                    transactionNode.put("senderIBAN", transaction.getSenderIBAN());
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionNode.put("transferType", transaction.getTransferType());
                    transactionsArray.add(transactionNode);
                } else if (transaction.getTransferType().equals("splitFailed")) {
                    transactionNode.put("amount", transaction.getAmount());
                    transactionNode.put("currency", transaction.getCurrency());
                    transactionNode.put("description", transaction.getDescription());
                    transactionNode.put("error", transaction.getError());
                    ArrayNode involvedAccounts = mapper.createArrayNode();
                    for (String involvedAccount : transaction.getInvolvedAccounts()) {
                        involvedAccounts.add(involvedAccount);
                    }
                    transactionNode.set("involvedAccounts", involvedAccounts);
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionsArray.add(transactionNode);
                } else if (transaction.getTransferType().equals("interest")) {
                    transactionNode.put("description", transaction.getDescription());
                    transactionNode.put("timestamp", transaction.getTimestamp());
                    transactionsArray.add(transactionNode);
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
//                                card.setStatus("frozen");
                                Transaction transaction = new Transaction(timestamp,
                                        "The card is frozen", account.getIban(), account.getIban(),
                                        0, "statusChange");
//                                account.getTransactions().add(transaction);
                            } else if (balanceDifference <= MIN_BALANCE_WARNING) {
                                card.setStatus("warning");
                                Transaction transaction = new Transaction(timestamp,
                                        "You have reached the minimum amount of funds,"
                                                + " the card will be frozen", account.getIban(),
                                        account.getIban(), 0, "statusChange");
                                account.getTransactions().add(transaction);
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
            result.set("output", outputNode);
            result.put("timestamp", timestamp);
            output.add(result);
        }
    }

    private void changeInterestRate(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        String accountIBAN = command.getAccount();
        double newInterestRate = command.getInterestRate();

        for (User user : users) {
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

    private void splitPayment(final CommandInput command) {
        List<String> accountsForSplit = command.getAccounts();
        int timestamp = command.getTimestamp();
        String currency = command.getCurrency();
        double amount = command.getAmount();

        double splitAmount = amount / accountsForSplit.size();
        boolean canSplit = true;

        String insufficientFundsAccount = null;
        for (String accountIBAN : accountsForSplit) {
            Account account = findAccountByIBAN(accountIBAN);
            if (account != null) {
                double conversionRate = convert(currency, account.getCurrency(),
                        new HashSet<>());
                double convertedSplitAmount = splitAmount * conversionRate;

                if (account.getBalance() < convertedSplitAmount) {
                    canSplit = false;
                    insufficientFundsAccount = accountIBAN;
                }
            }
        }

        for (String accountIBAN : accountsForSplit) {
            Account account = findAccountByIBAN(accountIBAN);
            if (account != null) {
                if (canSplit) {
                    double conversionRate = convert(currency, account.getCurrency(),
                            new HashSet<>());
                    double convertedAmount = splitAmount * conversionRate;
                    account.setBalance(account.getBalance() - convertedAmount);
                    String description = String.format("Split payment of %.2f %s",
                            amount, currency);
                    Transaction transaction = new Transaction(timestamp, description,
                            account.getIban(), account.getIban(), splitAmount, "split");
                    transaction.setInvolvedAccounts(accountsForSplit);
                    transaction.setCurrency(currency);
                    account.getTransactions().add(transaction);
                } else {
                    String description = String.format("Split payment of %.2f %s",
                            amount, currency);
                    String error = "Account " + insufficientFundsAccount
                            + " has insufficient funds for a split payment.";
                    Transaction transaction = new Transaction(timestamp, description,
                            account.getIban(), account.getIban(), splitAmount, "splitFailed");
                    transaction.setInvolvedAccounts(accountsForSplit);
                    transaction.setCurrency(currency);
                    transaction.setError(error);
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
                    ObjectNode transactionNode = mapper.createObjectNode();

                    if (transaction.getTransferType().equals("card")) {
                        transactionNode.put("description", transaction.getDescription());
                        transactionNode.put("timestamp", transaction.getTimestamp());
                        transactionNode.put("account", accountIBAN.getIban());
                        transactionNode.put("card", transaction.getReceiverIBAN());
                        transactionNode.put("cardHolder", findUserByAccount(accountIBAN.getIban())
                                .getEmail());
                    } else if (transaction.getTransferType().equals("online")) {
                        transactionNode.put("description", transaction.getDescription());
                        transactionNode.put("timestamp", transaction.getTimestamp());
                        transactionNode.put("amount", transaction.getAmount());
                        transactionNode.put("commerciant", transaction.getReceiverIBAN());
                    } else if (transaction.getTransferType().equals("sent")) {
                        transactionNode.put("amount", transaction.getAmount() + " "
                                + accountIBAN.getCurrency());
                        transactionNode.put("description", transaction.getDescription());
                        transactionNode.put("receiverIBAN", transaction.getReceiverIBAN());
                        transactionNode.put("senderIBAN", transaction.getSenderIBAN());
                        transactionNode.put("timestamp", transaction.getTimestamp());
                        transactionNode.put("transferType", transaction.getTransferType());
                    } else if (transaction.getTransferType().equals("account")) {
                        transactionNode.put("description", transaction.getDescription());
                        transactionNode.put("timestamp", transaction.getTimestamp());
                    } else if (transaction.getTransferType().equals("received")) {
                        transactionNode.put("amount", transaction.getAmount() + " "
                                + accountIBAN.getCurrency());
                        transactionNode.put("description", transaction.getDescription());
                        transactionNode.put("receiverIBAN", transaction.getReceiverIBAN());
                        transactionNode.put("senderIBAN", transaction.getSenderIBAN());
                        transactionNode.put("timestamp", transaction.getTimestamp());
                        transactionNode.put("transferType", transaction.getTransferType());
                    } else if (transaction.getTransferType().equals("splitFailed")) {
                        transactionNode.put("amount", transaction.getAmount());
                        transactionNode.put("currency", transaction.getCurrency());
                        transactionNode.put("description", transaction.getDescription());
                        transactionNode.put("error", transaction.getError());
                        ArrayNode involvedAccounts = mapper.createArrayNode();
                        for (String involvedAccount : transaction.getInvolvedAccounts()) {
                            involvedAccounts.add(involvedAccount);
                        }
                        transactionNode.set("involvedAccounts", involvedAccounts);
                        transactionNode.put("timestamp", transaction.getTimestamp());
                    } else if (transaction.getTransferType().equals("sentFailed")) {
                        transactionNode.put("description", transaction.getDescription());
                        transactionNode.put("timestamp", transaction.getTimestamp());
                    }

                    transactions.add(transactionNode);
                }
            }

            report.set("transactions", transactions);
            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            result.set("output", report);
            result.put("timestamp", command.getTimestamp());
            output.add(result);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", command.getTimestamp());
            result.set("output", outputNode);
            result.put("timestamp", command.getTimestamp());
            output.add(result);
        }
    }

    private void spendingsReport(final CommandInput command, final ArrayNode output) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String account = command.getAccount();

        Account accountIBAN = findAccountByIBAN(account);
        if (accountIBAN != null) {
            if (accountIBAN instanceof SavingsAccount) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode result = mapper.createObjectNode();
                result.put("command", command.getCommand());
                ObjectNode outputNode = mapper.createObjectNode();
                outputNode.put("error",
                        "This kind of report is not supported for a saving account");

                result.set("output", outputNode);
                result.put("timestamp", command.getTimestamp());
                output.add(result);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode report = mapper.createObjectNode();
            report.put("IBAN", accountIBAN.getIban());
            report.put("balance", accountIBAN.getBalance());
            report.put("currency", accountIBAN.getCurrency());

            ObjectNode spendings = mapper.createObjectNode();
            ArrayNode transactionsArray = mapper.createArrayNode();

            for (Transaction transaction : accountIBAN.getTransactions()) {
                if (transaction.getTimestamp() >= startTimestamp
                        && transaction.getTimestamp() <= endTimestamp) {
                    if (transaction.getTransferType().equals("online")) {
                        String commerciant = transaction.getReceiverIBAN();
                        double amount = transaction.getAmount();
                        if (spendings.has(commerciant)) {
                            spendings.put(commerciant, spendings.get(commerciant).asDouble()
                                    + amount);
                        } else {
                            spendings.put(commerciant, amount);
                        }

                        ObjectNode transactionNode = mapper.createObjectNode();
                        transactionNode.put("amount", amount);
                        transactionNode.put("commerciant", commerciant);
                        transactionNode.put("description", transaction.getDescription());
                        transactionNode.put("timestamp", transaction.getTimestamp());
                        transactionsArray.add(transactionNode);
                    }
                }
            }

            ArrayNode commerciantsArray = mapper.createArrayNode();
            List<ObjectNode> commerciantNodes = new ArrayList<>();
            for (Iterator<String> it = spendings.fieldNames(); it.hasNext();) {
                String commerciant = it.next();
                ObjectNode commerciantNode = mapper.createObjectNode();
                commerciantNode.put("commerciant", commerciant);
                commerciantNode.put("total", spendings.get(commerciant).asDouble());
                commerciantNodes.add(commerciantNode);
            }
            commerciantNodes.sort(Comparator.comparing(node -> node.get("commerciant").asText()));
            commerciantNodes.forEach(commerciantsArray::add);

            report.set("commerciants", commerciantsArray);
            report.set("transactions", transactionsArray);
            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            result.set("output", report);
            result.put("timestamp", command.getTimestamp());
            output.add(result);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            result.put("command", command.getCommand());
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", command.getTimestamp());
            result.set("output", outputNode);
            result.put("timestamp", command.getTimestamp());
            output.add(result);
        }
    }

    private void addInterest(final CommandInput command, final ArrayNode output) {
        String account = command.getAccount();
        int timestamp = command.getTimestamp();

        Account accountIBAN = findAccountByIBAN(account);
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

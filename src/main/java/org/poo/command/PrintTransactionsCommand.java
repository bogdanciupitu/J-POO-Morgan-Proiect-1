package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.classes.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PrintTransactionsCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        String email = command.getEmail();
        int commandTimestamp = command.getTimestamp();

        User user = bank.findUser(email);
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
                            + bank.findAccountByIBAN(transaction.getSenderIBAN()).getCurrency());
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
                            * bank.findAccountByIBAN(transaction.getInvolvedAccounts().getFirst())
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
                            + bank.findAccountByIBAN(transaction.getReceiverIBAN()).getCurrency());
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
}

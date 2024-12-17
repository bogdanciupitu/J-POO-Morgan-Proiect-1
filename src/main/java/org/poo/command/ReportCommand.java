package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.fileio.CommandInput;

public final class ReportCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String account = command.getAccount();

        Account accountIBAN = bank.findAccountByIBAN(account);
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
                        transactionNode.put("cardHolder",
                                bank.findUserByAccount(accountIBAN.getIban()).getEmail());
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
}

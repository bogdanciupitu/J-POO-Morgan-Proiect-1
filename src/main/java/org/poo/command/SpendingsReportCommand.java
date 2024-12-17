package org.poo.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.classes.Account;
import org.poo.classes.Bank;
import org.poo.classes.Transaction;
import org.poo.classes.SavingsAccount;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class SpendingsReportCommand implements Command {
    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Bank bank = Bank.getInstance();
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String account = command.getAccount();

        Account accountIBAN = bank.findAccountByIBAN(account);
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
}

package org.poo.classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {
    private int timestamp;
    private String description;
    private String senderIBAN;
    private String receiverIBAN;
    private double amount;
    private String transferType;

    public Transaction(final int timestamp, final String description, final String senderIBAN,
                       final String receiverIBAN, final double amount, final String transferType) {
        this.timestamp = timestamp;
        this.description = description;
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.transferType = transferType;
    }
}

package org.poo.classes;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.ExchangeInput;

import java.util.ArrayList;

@Getter
@Setter
public class Exchange {
    private String from;
    private String to;
    private double rate;
    private ArrayList<Transaction> transactions;

    public Exchange(final ExchangeInput exchangeInput) {
        this.from = exchangeInput.getFrom();
        this.to = exchangeInput.getTo();
        this.rate = exchangeInput.getRate();
        this.transactions = new ArrayList<>();
    }
}

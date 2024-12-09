package org.poo.classes;

import java.util.ArrayList;

public class ClassicAccount extends Account {
    public ClassicAccount(final String currency, final String accountType,
                          final int timestamp, final double balance, final String iban,
                          final ArrayList<Card> cards) {
        super(currency, accountType, timestamp, balance, iban, cards, new ArrayList<>());
    }
}

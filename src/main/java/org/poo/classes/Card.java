package org.poo.classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    private String cardNumber;
    private String status;

    public Card(final String cardNumber, final String status) {
        this.cardNumber = cardNumber;
        this.status = status;
    }
}

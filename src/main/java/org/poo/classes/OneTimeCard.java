package org.poo.classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OneTimeCard extends Card {
    private boolean used;

    public OneTimeCard(final String cardNumber, final String status) {
        super(cardNumber, status);
        this.used = false;
    }
}

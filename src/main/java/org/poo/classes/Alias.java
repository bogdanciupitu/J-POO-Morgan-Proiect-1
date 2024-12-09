package org.poo.classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alias {
    private String alias;
    private String account;

    public Alias(final String alias, final String account) {
        this.alias = alias;
        this.account = account;
    }
}

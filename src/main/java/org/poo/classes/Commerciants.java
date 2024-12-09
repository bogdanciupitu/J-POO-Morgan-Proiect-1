package org.poo.classes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Commerciants {
    private int id;
    private String description;
    private List<String> commerciants;

    public Commerciants(final int id, final String description, final List<String> commerciants) {
        this.id = id;
        this.description = description;
        this.commerciants = commerciants;
    }
}

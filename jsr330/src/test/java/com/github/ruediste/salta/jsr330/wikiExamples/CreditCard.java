package com.github.ruediste.salta.jsr330.wikiExamples;

public class CreditCard {

    public String owner;

    public CreditCard(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return owner;
    }
}

package com.github.ruediste.salta.jsr330.wikiExamples;

public class Order {

    public String description;
    public double amount;

    public Order(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return description + " => " + amount;
    }
}

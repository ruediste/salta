package com.github.ruediste.salta.jsr330.test.wikiExamples;

public class PaypalCreditCardProcessor implements CreditCardProcessor {

    @Override
    public void charge(CreditCard card, double amount) {
        System.out.println("PaypalCreditCardProcessor charged " + amount + " to " + card);
    }

}

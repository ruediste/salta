package com.github.ruediste.salta.jsr330.wikiExamples;

import javax.inject.Inject;

public class RealBillingService implements BillingService {

    @Inject
    @PayPal
    CreditCardProcessor processor;

    @Inject
    TransactionLog log;

    @Override
    public void bill(Order order, CreditCard card) {
        processor.charge(card, order.amount);
        log.log("Real Billing Service billed " + order + " to " + card);
    }
}
package com.github.ruediste.salta.jsr330.wikiExamples;

import com.github.ruediste.salta.jsr330.AbstractModule;

public class BillingModule extends AbstractModule {
    @Override
    protected void configure() {

        /*
         * This tells Salta that whenever it sees a dependency on a
         * TransactionLog, it should satisfy the dependency using a
         * DatabaseTransactionLog.
         */
        bind(TransactionLog.class).to(DatabaseTransactionLog.class);

        /*
         * Similarly, this binding tells Salta that when CreditCardProcessor is
         * used in a dependency, that should be satisfied with a
         * PaypalCreditCardProcessor.
         */
        bind(CreditCardProcessor.class).annotatedWith(PayPal.class).to(
                PaypalCreditCardProcessor.class);
    }
}
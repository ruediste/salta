package com.github.ruediste.salta.jsr330.wikiExamples;

import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;

public class Main {
    public static void main(String[] args) {
        /*
         * Salta.createInjector() takes your Modules, and returns a new Injector
         * instance. Most applications will call this method exactly once, in
         * their main() method.
         */
        Injector injector = Salta.createInjector(new BillingModule());

        /*
         * Now that we've got the injector, we can build objects.
         */
        BillingService billingService = injector.getInstance(BillingService.class);

        billingService.bill(new Order("Napkins", 10), new CreditCard("John Doe"));
    }
}

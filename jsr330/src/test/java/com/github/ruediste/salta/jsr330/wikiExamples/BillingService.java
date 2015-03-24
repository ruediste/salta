package com.github.ruediste.salta.jsr330.wikiExamples;

import com.github.ruediste.salta.jsr330.ImplementedBy;

@ImplementedBy(RealBillingService.class)
public interface BillingService {

	void bill(Order order, CreditCard creditCard);

}

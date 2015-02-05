package com.github.ruediste.simpledi.jsr330;

import com.github.ruediste.simpledi.AbstractModule;

public class JSR330Module extends AbstractModule {

	@Override
	protected void configure() {
		binder().getConfiguration().instantiatorRules
				.add(new JSR330ConstructorInstantiatorRule());
	}

}

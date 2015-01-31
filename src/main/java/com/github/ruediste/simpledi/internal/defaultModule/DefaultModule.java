package com.github.ruediste.simpledi.internal.defaultModule;

import com.github.ruediste.simpledi.AbstractModule;

public class DefaultModule extends AbstractModule {

	@Override
	protected void configure() {
		addRule(new ConstructorRule());
		addRule(new FieldInjectionRule());
		addRule(new DefaultScopeRule());
	}

}

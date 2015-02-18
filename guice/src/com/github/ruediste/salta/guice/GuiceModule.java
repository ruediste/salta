package com.github.ruediste.salta.guice;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.guice.binder.GuiceInjectorConfiguration;

public class GuiceModule extends AbstractModule {

	private GuiceInjectorConfiguration config;

	public GuiceModule(GuiceInjectorConfiguration config) {
		this.config = config;
	}

	@Override
	protected void configure() {

	}

}

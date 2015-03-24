package com.github.ruediste.salta.guice.binder;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.binder.StandardBinder;
import com.github.ruediste.salta.standard.binder.StandardBindingBuilderImpl;
import com.google.common.reflect.TypeToken;

public class StandardBinderGuice extends StandardBinder {

	private GuiceInjectorConfiguration guiceConfig;

	public StandardBinderGuice(GuiceInjectorConfiguration guiceConfig,
			StandardInjector injector) {
		super(guiceConfig.config, injector);
		this.guiceConfig = guiceConfig;
	}

	@Override
	protected <T> StandardBindingBuilderImpl<T> createBindingBuilder(
			TypeToken<T> type) {
		return new StandardBindingBuilderImplGuice<>(
				CoreDependencyKey.typeMatcher(type), type, guiceConfig,
				injector);
	}
}

package com.github.ruediste.salta.guice.binder;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.binder.StandardBinder;
import com.github.ruediste.salta.standard.binder.StandardBindingBuilderImpl;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

public class StandardBinderGuice extends StandardBinder {

	public StandardBinderGuice(StandardInjectorConfiguration config,
			StandardInjector injector) {
		super(config, injector);
	}

	@Override
	protected <T> StandardBindingBuilderImpl<T> createBindingBuilder(
			TypeToken<T> type) {
		return new StandardBindingBuilderImplGuice<>(
				CoreDependencyKey.typeMatcher(type), type, config, injector);
	}
}

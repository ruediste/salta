package com.github.ruediste.salta.guice.binder;

import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.CreationRecipeFactory;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.binder.StandardBindingBuilderImpl;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

public class StandardBindingBuilderImplGuice<T> extends
		StandardBindingBuilderImpl<T> {

	public StandardBindingBuilderImplGuice(
			Matcher<CoreDependencyKey<?>> typeMatcher, TypeToken<T> type,
			StandardInjectorConfiguration config, StandardInjector injector) {
		super(typeMatcher, type, config, injector);
	}

	@Override
	protected <P> Supplier<CreationRecipeFactory> createProviderRecipeFactorySupplier(
			CoreDependencyKey<P> providerKey,
			Function<? super P, ? extends T> providerWrapper) {
		Supplier<CreationRecipeFactory> inner = super
				.createProviderRecipeFactorySupplier(providerKey,
						providerWrapper);
		return () -> {
			config.implicitlyBoundKeys.add(providerKey);
			return inner.get();
		};
	}
}

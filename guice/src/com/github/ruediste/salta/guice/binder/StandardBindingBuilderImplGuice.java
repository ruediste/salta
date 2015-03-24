package com.github.ruediste.salta.guice.binder;

import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.CreationRecipeFactory;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.binder.StandardBindingBuilderImpl;
import com.google.common.reflect.TypeToken;

public class StandardBindingBuilderImplGuice<T> extends
		StandardBindingBuilderImpl<T> {

	private GuiceInjectorConfiguration guiceConfig;

	public StandardBindingBuilderImplGuice(
			Matcher<CoreDependencyKey<?>> typeMatcher, TypeToken<T> type,
			GuiceInjectorConfiguration guiceConfig, StandardInjector injector) {
		super(typeMatcher, type, guiceConfig.config, injector);
		this.guiceConfig = guiceConfig;
	}

	@Override
	protected <P> Supplier<CreationRecipeFactory> createProviderRecipeFactorySupplier(
			CoreDependencyKey<P> providerKey,
			Function<? super P, ? extends T> providerWrapper) {
		Supplier<CreationRecipeFactory> inner = super
				.createProviderRecipeFactorySupplier(providerKey,
						providerWrapper);
		return () -> {
			guiceConfig.implicitlyBoundKeys.add(providerKey);
			return inner.get();
		};
	}

	@Override
	protected Supplier<CreationRecipeFactory> createDefaultCreationRecipeFactorySupplier(
			TypeToken<? extends T> implementation) {
		Supplier<CreationRecipeFactory> inner = super
				.createDefaultCreationRecipeFactorySupplier(implementation);
		return () -> {
			guiceConfig.typesBoundToDefaultCreationRecipe.add(implementation);
			return inner.get();
		};
	}
}

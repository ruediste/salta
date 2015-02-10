package com.github.ruediste.salta.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Provider;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.InstantiationContext;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.google.common.reflect.TypeToken;

public class StandardInjector implements Injector {

	private final class ProviderImpl<T> implements Provider<T> {

		private Function<InstantiationContext, T> factoryFunction;

		public ProviderImpl(CoreDependencyKey<T> key) {
			config.staticInitializers.add(i -> factoryFunction = injector
					.getInstanceFactory(key));
		}

		@Override
		public T get() {
			checkInitialized();
			return factoryFunction.apply(new InstantiationContext(injector));
		}
	}

	private final class MembersInjectorImpl<T> implements MembersInjector<T> {
		List<RecipeMembersInjector<T>> injectors = new ArrayList<>();

		public MembersInjectorImpl(TypeToken<T> type) {
			config.staticInitializers.add(i -> injectors = config
					.createRecipeMembersInjectors(type));
		}

		@Override
		public void injectMembers(T instance) {
			checkInitialized();
			for (RecipeMembersInjector<T> i : injectors) {
				i.injectMembers(instance, null);
			}
		}
	}

	private boolean initialized;
	private StandardInjectorConfiguration config;
	private CoreInjector injector;

	private void checkInitialized() {
		if (!initialized) {
			throw new ProvisionException(
					"Cannot use injector before it is initialized");
		}
	}

	public void initialize(StandardInjectorConfiguration config) {
		this.config = config;
		injector = new CoreInjector(config.config);
		for (Consumer<Injector> initializer : config.staticInitializers) {
			initializer.accept(this);
		}
		initialized = true;
		for (Consumer<Injector> initializer : config.dynamicInitializers) {
			initializer.accept(this);
		}
	}

	@Override
	public void injectMembers(Object instance) {
		injectMembers(instance, injector.createContextualInjector());
	}

	@Override
	public <T> void injectMembers(TypeToken<T> type, T instance) {
		injectMembers(type, instance, injector.createContextualInjector());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void injectMembers(Object instance,
			ContextualInjector contextualInjector) {
		injectMembers((TypeToken) TypeToken.of(instance.getClass()), instance,
				contextualInjector);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> void injectMembers(TypeToken<T> type, T instance,
			ContextualInjector contextualInjector) {
		checkInitialized();
		List<RecipeMembersInjector<Object>> injectors = (List) config
				.createRecipeMembersInjectors(type);
		for (RecipeMembersInjector<Object> rmi : injectors) {
			rmi.injectMembers(instance, contextualInjector);
		}
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(TypeToken<T> typeLiteral) {
		return new MembersInjectorImpl<T>(typeLiteral);
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		return getMembersInjector(TypeToken.of(type));
	}

	@Override
	public <T> Provider<T> getProvider(CoreDependencyKey<T> key) {

		return new ProviderImpl<T>(key);
	}

	@Override
	public <T> Provider<T> getProvider(Class<T> type) {
		return getProvider(DependencyKey.of(type));
	}

	@Override
	public <T> T getInstance(CoreDependencyKey<T> key) {

		checkInitialized();
		return injector.getInstance(key);
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		checkInitialized();
		return injector.getInstance(DependencyKey.of(type));
	}

}

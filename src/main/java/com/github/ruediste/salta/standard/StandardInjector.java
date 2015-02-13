package com.github.ruediste.salta.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Provider;

import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.core.BindingContextImpl;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.TransitiveMembersInjector;
import com.google.common.reflect.TypeToken;

public class StandardInjector implements Injector {

	private final class ProviderImpl<T> implements Provider<T> {

		private Function<BindingContext, T> factoryFunction;

		public ProviderImpl(CoreDependencyKey<T> key) {
			config.staticInitializers.add(i -> factoryFunction = coreInjector
					.getInstanceFactory(key));
		}

		@Override
		public T get() {
			checkInitialized();
			return factoryFunction.apply(new BindingContextImpl(coreInjector));
		}
	}

	private final class MembersInjectorImpl<T> implements MembersInjector<T> {
		List<TransitiveMembersInjector> injectors = new ArrayList<>();

		public MembersInjectorImpl(TypeToken<T> type) {
			config.staticInitializers.add(i -> injectors = config
					.createRecipeMembersInjectors(new BindingContextImpl(
							coreInjector), type));
		}

		@Override
		public void injectMembers(T instance) {
			checkInitialized();
			for (TransitiveMembersInjector i : injectors) {
				i.injectMembers(instance);
			}
		}
	}

	private boolean initialized;
	private StandardInjectorConfiguration config;
	private CoreInjector coreInjector;

	private void checkInitialized() {
		if (!initialized) {
			throw new ProvisionException(
					"Cannot use injector before it is initialized");
		}
	}

	public void initialize(StandardInjectorConfiguration config) {
		this.config = config;
		coreInjector = new CoreInjector(config.config);
		for (Consumer<Injector> initializer : config.staticInitializers) {
			initializer.accept(this);
		}
		initialized = true;
		for (Consumer<Injector> initializer : config.dynamicInitializers) {
			initializer.accept(this);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void injectMembers(Object instance) {
		injectMembers((TypeToken) TypeToken.of(instance.getClass()), instance);
	}

	@Override
	public <T> void injectMembers(TypeToken<T> type, T instance) {
		checkInitialized();
		List<TransitiveMembersInjector> injectors = config
				.createRecipeMembersInjectors(new BindingContextImpl(
						coreInjector), type);
		for (TransitiveMembersInjector rmi : injectors) {
			rmi.injectMembers(instance);
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
		return coreInjector.getInstance(key);
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		checkInitialized();
		return coreInjector.getInstance(DependencyKey.of(type));
	}

}

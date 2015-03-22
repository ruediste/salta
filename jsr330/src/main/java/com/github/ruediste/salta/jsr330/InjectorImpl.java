package com.github.ruediste.salta.jsr330;

import java.util.function.Supplier;

import javax.inject.Provider;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.MembersInjector;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.config.MembersInjectionToken;
import com.google.common.reflect.TypeToken;

public class InjectorImpl implements Injector {

	private StandardInjector delegate;

	public InjectorImpl(JSR330InjectorConfiguration config) {
		this.delegate = new StandardInjector(config.config);
	}

	public void initialize() {
		delegate.initialize();
	}

	@Override
	public void injectMembers(Object instance) {
		delegate.injectMembers(instance);
	}

	@Override
	public <T> void injectMembers(TypeToken<T> type, T instance) {
		delegate.injectMembers(type, instance);
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(TypeToken<T> typeLiteral) {
		return delegate.getMembersInjector(typeLiteral);
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		return delegate.getMembersInjector(type);
	}

	@Override
	public <T> Provider<T> getProvider(CoreDependencyKey<T> key) {

		Supplier<T> result = delegate.getProvider(key);
		return new Provider<T>() {

			@Override
			public T get() {
				return result.get();
			}

			@Override
			public String toString() {
				return result.toString();
			}
		};
	}

	@Override
	public <T> Provider<T> getProvider(Class<T> type) {
		Supplier<T> result = delegate.getProvider(type);
		return new Provider<T>() {

			@Override
			public T get() {
				return result.get();
			}

			@Override
			public String toString() {
				return result.toString();
			}
		};
	}

	@Override
	public <T> T getInstance(CoreDependencyKey<T> key) {
		return delegate.getInstance(key);
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		return delegate.getInstance(type);
	}

	@Override
	public <T> MembersInjectionToken<T> getMembersInjectionToken(T value) {
		return delegate.getMembersInjectionToken(value);
	}

	@Override
	public <T> MembersInjectionToken<T> getMembersInjectionToken(T value,
			TypeToken<T> type) {
		return delegate.getMembersInjectionToken(value, type);
	}

	@Override
	public StandardInjector getDelegate() {
		return delegate;
	}

}

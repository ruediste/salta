package com.github.ruediste.salta.guice;

import com.github.ruediste.salta.guice.binder.BindingImpl;
import com.github.ruediste.salta.guice.binder.GuiceInjectorConfiguration;
import com.github.ruediste.salta.standard.StandardInjector;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

public class GuiceInjectorImpl implements Injector {

	private StandardInjector delegate;

	public GuiceInjectorImpl(GuiceInjectorConfiguration config) {
		delegate = new StandardInjector(config.config);
	}

	@Override
	public void injectMembers(Object instance) {
		delegate.injectMembers(instance);
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
		return delegate.getMembersInjector(typeLiteral.getTypeToken())::injectMembers;
	}

	@Override
	public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		com.github.ruediste.salta.standard.MembersInjector<T> membersInjector = delegate
				.getMembersInjector(type);
		return new MembersInjector<T>() {
			@Override
			public void injectMembers(T i) {
				membersInjector.injectMembers(i);
			}

			@Override
			public String toString() {
				return membersInjector.toString();
			}
		};
	}

	@Override
	public <T> Provider<T> getProvider(Key<T> key) {
		javax.inject.Provider<T> provider = delegate
				.getProvider(new KeyAdapter<>(key));
		return new Provider<T>() {
			@Override
			public T get() {
				return provider.get();
			}

			@Override
			public String toString() {
				return provider.toString();
			}
		};
	}

	@Override
	public <T> Provider<T> getProvider(Class<T> type) {
		javax.inject.Provider<T> provider = delegate.getProvider(type);
		return new Provider<T>() {
			@Override
			public T get() {
				return provider.get();
			}

			@Override
			public String toString() {
				return provider.toString();
			}
		};
	}

	@Override
	public <T> T getInstance(Key<T> key) {
		return delegate.getInstance(new KeyAdapter<>(key));
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		return delegate.getInstance(type);
	}

	@Override
	public <T> Binding<T> getBinding(Key<T> key) {
		return new BindingImpl<>(key, getProvider(key));
	}

	@Override
	public <T> Binding<T> getBinding(Class<T> type) {

		return new BindingImpl<>(Key.get(type), getProvider(type));
	}

	@Override
	public com.github.ruediste.salta.standard.Injector getSaltaInjector() {
		return delegate;
	}

	public void initialize() {
		delegate.initialize();
	}

}

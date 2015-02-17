package com.github.ruediste.salta.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

public class GuiceInjectorImpl implements Injector {

	com.github.ruediste.salta.standard.Injector delegate;

	public GuiceInjectorImpl(
			com.github.ruediste.salta.standard.Injector delegate) {
		this.delegate = delegate;
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
		return delegate.getMembersInjector(type)::injectMembers;
	}

	@Override
	public <T> Provider<T> getProvider(Key<T> key) {
		return delegate.getProvider(new KeyAdapter<>(key))::get;
	}

	@Override
	public <T> Provider<T> getProvider(Class<T> type) {
		return delegate.getProvider(type)::get;
	}

	@Override
	public <T> T getInstance(Key<T> key) {
		return delegate.getInstance(new KeyAdapter<>(key));
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		return delegate.getInstance(type);
	}

}

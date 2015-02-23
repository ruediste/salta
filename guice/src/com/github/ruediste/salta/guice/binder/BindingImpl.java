package com.github.ruediste.salta.guice.binder;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * {@link Binding} implementation implementing the {@link #getKey()} method
 */
public class BindingImpl<T> implements Binding<T> {

	private Key<?> key;
	private Provider<?> provider;

	public BindingImpl(Key<?> key, Provider<?> provider) {
		this.key = key;
		this.provider = provider;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Key<T> getKey() {
		return (Key<T>) key;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Provider<T> getProvider() {
		return (Provider<T>) provider;
	}

}

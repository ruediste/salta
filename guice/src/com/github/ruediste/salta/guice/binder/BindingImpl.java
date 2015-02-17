package com.github.ruediste.salta.guice.binder;

import com.google.common.reflect.TypeToken;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * {@link Binding} implementation implementing the {@link #getKey()} method
 */
public class BindingImpl<T> implements Binding<T> {

	private TypeToken<?> type;

	public BindingImpl(TypeToken<?> type) {
		this.type = type;
	}

	@Override
	public Object getSource() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Key<T> getKey() {
		return (Key<T>) Key.get(type.getType());
	}

	@Override
	public Provider<T> getProvider() {
		throw new UnsupportedOperationException();
	}

}

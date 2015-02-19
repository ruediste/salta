package com.github.ruediste.salta.guice.binder;

import java.util.function.Supplier;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.standard.ScopeImpl.ScopeHandler;
import com.google.common.reflect.TypeToken;
import com.google.inject.Scope;

public class GuiceScopeAdapter implements ScopeHandler {

	private Scope scope;

	public GuiceScopeAdapter(Scope scope) {
		this.scope = scope;
	}

	@Override
	public Supplier<Object> scope(Supplier<Object> supplier, Binding binding,
			TypeToken<?> type) {
		return doScope(supplier, binding, type);
	}

	@SuppressWarnings("unchecked")
	private <T> Supplier<T> doScope(Supplier<Object> supplier, Binding binding,
			TypeToken<?> type) {
		return scope.scope(binding, (TypeToken<T>) type,
				() -> (T) supplier.get())::get;
	}

	@Override
	public String toString() {
		return scope.toString();
	}
}

package com.github.ruediste.salta.core;

import java.util.function.Supplier;

public class ContextualInjectorImpl implements ContextualInjector {

	private CoreInjector injector;

	public ContextualInjectorImpl(CoreInjector injector) {
		this.injector = injector;
	}

	@Override
	public <T> T withBinding(Binding binding, Supplier<T> sup) {
		return sup.get();
	}

	@Override
	public <T> T getInstance(CoreDependencyKey<T> key) {
		return null;
	}
}

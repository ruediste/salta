package com.github.ruediste.salta.core;

public class ContextualInjectorImpl implements ContextualInjector {

	private CoreInjector injector;

	public ContextualInjectorImpl(CoreInjector injector,
			InstantiationContext ctx) {
		this.injector = injector;
	}

	@Override
	public <T> T createInstance(CoreDependencyKey<T> key) {
		return injector.getInstance(key, this);
	}

	@Override
	public CoreInjector getInjector() {
		return injector;
	}

}

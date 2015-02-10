package com.github.ruediste.salta.core;

public class ContextualInjectorImpl implements ContextualInjector {

	private CoreInjector injector;
	private InstantiationContext ctx;

	public ContextualInjectorImpl(CoreInjector injector,
			InstantiationContext ctx) {
		this.injector = injector;
		this.ctx = ctx;
	}

	@Override
	public <T> T getInstance(CoreDependencyKey<T> key) {
		return injector.getInstance(key, ctx);
	}

	@Override
	public CoreInjector getInjector() {
		return injector;
	}

	@Override
	public InstantiationContext getInstantiationContext() {
		return ctx;
	}
}

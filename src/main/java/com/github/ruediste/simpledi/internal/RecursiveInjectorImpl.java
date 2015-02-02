package com.github.ruediste.simpledi.internal;

import com.github.ruediste.simpledi.InstantiationContext;
import com.github.ruediste.simpledi.InstantiationRequest;
import com.github.ruediste.simpledi.ContextualInjector;

public class RecursiveInjectorImpl implements ContextualInjector {

	private InjectorImpl injector;
	private InstantiationContext ctx;

	public RecursiveInjectorImpl(InjectorImpl injector, InstantiationContext ctx) {
		this.injector = injector;
		this.ctx = ctx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createInstance(InstantiationRequest request) {
		return (T) injector.createInstance(request, ctx);
	}
}

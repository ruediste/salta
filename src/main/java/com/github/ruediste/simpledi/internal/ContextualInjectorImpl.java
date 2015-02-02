package com.github.ruediste.simpledi.internal;

import com.github.ruediste.simpledi.ContextualInjector;
import com.github.ruediste.simpledi.Dependency;
import com.github.ruediste.simpledi.InstantiationContext;
import com.github.ruediste.simpledi.Instantiator;
import com.google.common.reflect.TypeToken;

public class ContextualInjectorImpl implements ContextualInjector {

	private InjectorImpl injector;
	private InstantiationContext ctx;

	public ContextualInjectorImpl(InjectorImpl injector,
			InstantiationContext ctx) {
		this.injector = injector;
		this.ctx = ctx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createInstance(Dependency<T> dependency) {
		return (T) injector.createInstance(dependency, ctx);
	}

	@Override
	public <T> Instantiator<T> createInstantiator(TypeToken<T> type) {
		return injector.createInstantiator(type);
	}
}

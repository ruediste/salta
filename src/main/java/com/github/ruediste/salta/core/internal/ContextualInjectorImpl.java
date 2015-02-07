package com.github.ruediste.salta.core.internal;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.core.Injector;
import com.github.ruediste.salta.core.InstantiationContext;
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void injectMembers(TypeToken<?> type, Object value) {
		injector.injectMembers((TypeToken) type, value);
	}

	@Override
	public Injector getInjector() {
		return injector;
	}

}

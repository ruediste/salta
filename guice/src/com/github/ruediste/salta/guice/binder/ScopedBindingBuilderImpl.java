package com.github.ruediste.salta.guice.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.standard.ScopeImpl;
import com.google.inject.Scope;
import com.google.inject.binder.ScopedBindingBuilder;

public class ScopedBindingBuilderImpl implements ScopedBindingBuilder {

	private com.github.ruediste.salta.standard.binder.StandardScopedBindingBuilder<?> delegate;

	public ScopedBindingBuilderImpl(
			com.github.ruediste.salta.standard.binder.StandardScopedBindingBuilder<?> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void in(Class<? extends Annotation> scopeAnnotation) {
		delegate.in(scopeAnnotation);
	}

	@Override
	public void in(Scope scope) {
		delegate.in(new ScopeImpl(new GuiceScopeAdapter(scope)));
	}

	@Override
	public void asEagerSingleton() {
		delegate.asEagerSingleton();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

}

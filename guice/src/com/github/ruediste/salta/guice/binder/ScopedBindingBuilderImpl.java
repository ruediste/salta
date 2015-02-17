package com.github.ruediste.salta.guice.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.standard.Scope;
import com.google.inject.binder.ScopedBindingBuilder;

public class ScopedBindingBuilderImpl implements ScopedBindingBuilder {

	private com.github.ruediste.salta.standard.binder.ScopedBindingBuilder<?> delegate;

	public ScopedBindingBuilderImpl(
			com.github.ruediste.salta.standard.binder.ScopedBindingBuilder<?> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void in(Class<? extends Annotation> scopeAnnotation) {
		delegate.in(scopeAnnotation);
	}

	@Override
	public void in(Scope scope) {
		delegate.in(scope);
	}

	@Override
	public void asEagerSingleton() {
		delegate.asEagerSingleton();
	}

}

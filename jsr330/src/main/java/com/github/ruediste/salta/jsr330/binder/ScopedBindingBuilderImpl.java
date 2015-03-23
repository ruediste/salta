package com.github.ruediste.salta.jsr330.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.core.Scope;

public class ScopedBindingBuilderImpl<T> implements ScopedBindingBuilder<T> {

	private com.github.ruediste.salta.standard.binder.StandardScopedBindingBuilder<T> delegate;

	public ScopedBindingBuilderImpl(
			com.github.ruediste.salta.standard.binder.StandardScopedBindingBuilder<T> delegate) {
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

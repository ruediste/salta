package com.github.ruediste.salta.guice.binder;

import java.lang.annotation.Annotation;

import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;

public class AnnotatedBindingBuilderImpl<T> extends LinkedBindingBuilderImpl<T>
		implements AnnotatedBindingBuilder<T> {

	private com.github.ruediste.salta.standard.binder.StandardAnnotatedBindingBuilder<T> delegate;

	public AnnotatedBindingBuilderImpl(
			com.github.ruediste.salta.standard.binder.StandardAnnotatedBindingBuilder<T> delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> annotationType) {
		return new LinkedBindingBuilderImpl<>(
				delegate.annotatedWith(annotationType));
	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
		return new LinkedBindingBuilderImpl<>(
				delegate.annotatedWith(annotation));
	}

}

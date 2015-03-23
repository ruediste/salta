package com.github.ruediste.salta.jsr330.binder;

import java.lang.annotation.Annotation;

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
			Class<? extends Annotation> availableAnnotationType) {
		return new LinkedBindingBuilderImpl<>(
				delegate.annotatedWith(availableAnnotationType));
	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(Annotation availableAnnotation) {
		return new LinkedBindingBuilderImpl<>(
				delegate.annotatedWith(availableAnnotation));
	}

}

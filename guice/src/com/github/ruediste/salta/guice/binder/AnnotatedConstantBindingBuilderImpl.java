package com.github.ruediste.salta.guice.binder;

import java.lang.annotation.Annotation;

import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.ConstantBindingBuilder;

public class AnnotatedConstantBindingBuilderImpl implements
		AnnotatedConstantBindingBuilder {

	private com.github.ruediste.salta.standard.binder.AnnotatedConstantBindingBuilder delegate;

	public AnnotatedConstantBindingBuilderImpl(
			com.github.ruediste.salta.standard.binder.AnnotatedConstantBindingBuilder delegate) {
		this.delegate = delegate;
	}

	@Override
	public ConstantBindingBuilder annotatedWith(
			Class<? extends Annotation> annotationType) {
		return new ConstantBindingBuilderImpl(
				delegate.annotatedWith(annotationType));
	}

	@Override
	public ConstantBindingBuilder annotatedWith(Annotation annotation) {
		return new ConstantBindingBuilderImpl(
				delegate.annotatedWith(annotation));
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}

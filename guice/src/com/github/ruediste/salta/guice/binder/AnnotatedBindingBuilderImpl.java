package com.github.ruediste.salta.guice.binder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import com.github.ruediste.salta.standard.Scope;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

public class AnnotatedBindingBuilderImpl<T> implements
		AnnotatedBindingBuilder<T> {

	private com.github.ruediste.salta.standard.binder.AnnotatedBindingBuilder<T> delegate;

	public AnnotatedBindingBuilderImpl(
			com.github.ruediste.salta.standard.binder.AnnotatedBindingBuilder<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public ScopedBindingBuilder to(Class<? extends T> implementation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScopedBindingBuilder to(Key<? extends T> targetKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toInstance(T instance) {
		// TODO Auto-generated method stub

	}

	@Override
	public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScopedBindingBuilder toProvider(
			javax.inject.Provider<? extends T> provider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScopedBindingBuilder toProvider(
			Class<? extends javax.inject.Provider<? extends T>> providerType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScopedBindingBuilder toProvider(
			TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScopedBindingBuilder toProvider(
			Key<? extends javax.inject.Provider<? extends T>> providerKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends T> ScopedBindingBuilder toConstructor(
			Constructor<S> constructor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends T> ScopedBindingBuilder toConstructor(
			Constructor<S> constructor, TypeLiteral<? extends S> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void in(Class<? extends Annotation> scopeAnnotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void asEagerSingleton() {
		// TODO Auto-generated method stub

	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> annotationType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void in(Scope scope) {
		// TODO Auto-generated method stub

	}

}

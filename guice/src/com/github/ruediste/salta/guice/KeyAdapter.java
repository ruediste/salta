package com.github.ruediste.salta.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.binder.AnnotationProxy;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;

public class KeyAdapter<T> extends CoreDependencyKey<T> {

	private Key<T> key;

	public KeyAdapter(Key<T> key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return key.toString();
	}

	public static <T> KeyAdapter<T> of(Key<T> key) {
		return new KeyAdapter<>(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeToken<T> getType() {
		return (TypeToken<T>) TypeToken.of(key.getTypeLiteral().getType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getRawType() {
		return (Class<T>) key.getTypeLiteral().getRawType();
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return new AnnotatedElement() {

			@Override
			public Annotation[] getDeclaredAnnotations() {
				Annotation a = key.getAnnotation();
				if (a != null)
					return new Annotation[] { a };
				else {
					Class<? extends Annotation> annotationType = key
							.getAnnotationType();
					if (annotationType != null)
						return new Annotation[] { AnnotationProxy
								.newProxy(annotationType) };
					else
						return new Annotation[] {};

				}
			}

			@Override
			public Annotation[] getAnnotations() {
				return getDeclaredAnnotations();
			}

			@SuppressWarnings({ "unchecked", "hiding" })
			@Override
			public <T extends Annotation> T getAnnotation(
					Class<T> annotationClass) {
				Class<? extends Annotation> annotationType = key
						.getAnnotationType();
				if (Objects.equals(annotationClass, annotationType)) {
					return (T) key.getAnnotation();
				} else
					return null;
			}
		};
	}

}

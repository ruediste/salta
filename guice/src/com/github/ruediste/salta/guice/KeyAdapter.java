package com.github.ruediste.salta.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;

public class KeyAdapter<T> extends CoreDependencyKey<T> {

	private Key<T> key;

	public KeyAdapter(Key<T> key) {
		this.key = key;
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
				if (a == null)
					return new Annotation[] {};
				else
					return new Annotation[] { a };
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

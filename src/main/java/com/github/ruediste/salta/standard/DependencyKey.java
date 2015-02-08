package com.github.ruediste.salta.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.binder.AnnotationProxy;
import com.google.common.reflect.TypeToken;

/**
 * The type and {@link InjectionPoint} to lookup an instance.
 */
public class DependencyKey<T> extends CoreDependencyKey<T> {
	private final TypeToken<T> type;
	private final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

	public static <T> DependencyKey<T> of(TypeToken<T> type) {
		return new DependencyKey<>(type);
	}

	public static <T> DependencyKey<T> of(Class<T> type) {
		return new DependencyKey<>(TypeToken.of(type));
	}

	private DependencyKey(TypeToken<T> type) {
		this.type = type;
	}

	@Override
	public TypeToken<T> getType() {
		return type;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return new AnnotatedElement() {

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return getAnnotations();
			}

			@Override
			public Annotation[] getAnnotations() {
				return annotations.values().toArray(new Annotation[] {});
			}

			@SuppressWarnings("unchecked")
			@Override
			public <A extends Annotation> A getAnnotation(
					Class<A> annotationClass) {
				return (A) annotations.get(annotationClass);
			}
		};
	}

	public Map<Class<? extends Annotation>, Annotation> getAnnotations() {
		return annotations;
	}

	public DependencyKey<T> addAnnotation(Annotation a) {
		annotations.put(a.annotationType(), a);
		return this;
	}

	public DependencyKey<T> addAnnotation(Class<? extends Annotation> cls) {
		annotations.put(cls, AnnotationProxy.newProxy(cls)
				.getProxedAnnotation());
		return this;
	}

	public CoreDependencyKey<T> addAnnotations(Annotation... as) {
		for (Annotation a : as)
			addAnnotation(a);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, annotations, getAttachedPropertyMap());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!Objects.equals(getClass(), obj.getClass()))
			return false;
		DependencyKey<?> other = (DependencyKey<?>) obj;
		return Objects.equals(type, other.type)
				&& Objects.equals(annotations, other.annotations)
				&& Objects.equals(getAttachedPropertyMap(),
						other.getAttachedPropertyMap());
	}
}

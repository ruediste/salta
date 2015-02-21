package com.github.ruediste.salta.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
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
	private Class<T> rawType;
	private final Map<Class<? extends Annotation>, Annotation> annotations;
	private final int hashCode;

	public static <T> DependencyKey<T> of(TypeToken<T> type) {
		return new DependencyKey<>(type, null, new HashMap<>());
	}

	public static <T> DependencyKey<T> of(TypeToken<T> type, Class<T> rawType) {
		return new DependencyKey<>(type, rawType, new HashMap<>());
	}

	public static <T> DependencyKey<T> of(Class<T> type) {
		return new DependencyKey<>(TypeToken.of(type), type, new HashMap<>());
	}

	private DependencyKey(TypeToken<T> type, Class<T> rawType,
			Map<Class<? extends Annotation>, Annotation> annotations) {
		this.type = type;
		this.rawType = rawType;
		this.annotations = annotations;

		// don't use Objects.hash() for performance reason
		this.hashCode = type.hashCode() + 31 * annotations.hashCode();
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
		return Collections.unmodifiableMap(annotations);
	}

	@SuppressWarnings("unchecked")
	public DependencyKey<T> withAnnotations(Class<? extends Annotation>... cls) {
		Annotation[] annotations = new Annotation[cls.length];
		for (int i = 0; i < cls.length; i++) {
			annotations[i] = AnnotationProxy.newProxy(cls[i]);
		}
		return withAnnotations(annotations);
	}

	public DependencyKey<T> withAnnotations(Annotation... additionalAnnotations) {
		HashMap<Class<? extends Annotation>, Annotation> tmp = new HashMap<>(
				annotations);
		for (Annotation a : additionalAnnotations)
			tmp.put(a.annotationType(), a);
		return new DependencyKey<>(type, rawType, tmp);
	}

	@Override
	public int hashCode() {
		return hashCode;
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
				&& Objects.equals(annotations, other.annotations);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getRawType() {
		// no synchronization required. In the worst case, multiple
		// threads will compute the value and overwrite each other's result,
		// which is No Problem
		if (rawType == null)
			rawType = (Class<T>) type.getRawType();
		return rawType;
	}

	@Override
	public String toString() {
		return "DependencyKey(" + getType() + "," + annotations.values() + ")";
	}
}

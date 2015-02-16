package com.github.ruediste.salta.jsr330;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Named;
import javax.inject.Qualifier;

public class JSR330ReflectionUtil {

	private JSR330ReflectionUtil() {
	}

	public static List<Annotation> getQualifiers(AnnotatedElement annotated) {
		ArrayList<Annotation> result = new ArrayList<>();
		for (Annotation annotation : annotated.getDeclaredAnnotations()) {
			if (isQualifier(annotation)) {
				result.add(annotation);
			}
		}
		return result;
	}

	public static boolean isQualifier(Annotation annotation) {
		return annotation.annotationType().isAnnotationPresent(Qualifier.class);
	}

	public static boolean areQualifiersMatching(
			List<Annotation> presentQualifiers,
			List<Annotation> requiredQualifiers) {
		outer: for (Annotation requiredQualifier : requiredQualifiers) {
			for (Annotation presentQualifier : presentQualifiers) {
				if (Objects.equals(requiredQualifier, presentQualifier))
					continue outer;
			}
			return false;
		}
		return true;
	}

	@SuppressWarnings("all")
	private static class NamedImpl implements Named, Serializable {

		private static final long serialVersionUID = 1L;
		private final String value;

		public NamedImpl(String value) {
			this.value = checkNotNull(value, "name");
		}

		@Override
		public String value() {
			return this.value;
		}

		@Override
		public int hashCode() {
			// This is specified in java.lang.Annotation.
			return (127 * "value".hashCode()) ^ value.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Named)) {
				return false;
			}

			Named other = (Named) o;
			return value.equals(other.value());
		}

		@Override
		public String toString() {
			return "@" + Named.class.getName() + "(value=" + value + ")";
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return Named.class;
		}
	}

	public static Named createNamed(String name) {
		return new NamedImpl(name);
	}
}

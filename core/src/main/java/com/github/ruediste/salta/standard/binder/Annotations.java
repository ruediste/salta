package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.matchers.Matcher;

public class Annotations {

	private Annotations() {
	}

	public static Matcher<CoreDependencyKey<?>> matcher(
			Class<? extends Annotation> annotationType) {
		return new Matcher<CoreDependencyKey<?>>() {
			@Override
			public boolean matches(CoreDependencyKey<?> d) {
				return d.getAnnotatedElement().isAnnotationPresent(
						annotationType);
			}

			@Override
			public String toString() {
				return "annotatedWith(" + annotationType.getName() + ")";
			}
		};
	}

	public static Matcher<CoreDependencyKey<?>> matcher(Annotation annotation) {
		return new Matcher<CoreDependencyKey<?>>() {
			@Override
			public boolean matches(CoreDependencyKey<?> d) {
				return Arrays.stream(d.getAnnotatedElement().getAnnotations())
						.anyMatch(a -> Objects.equals(annotation, a));
			}

			@Override
			public String toString() {
				return "annotatedWith(" + annotation + ")";
			}
		};
	}
}

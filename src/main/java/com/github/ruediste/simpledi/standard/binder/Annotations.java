package com.github.ruediste.simpledi.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.matchers.Matcher;

public class Annotations {

	/**
	 * Used to place an annotation on a dependency without an injection point
	 */
	public static AttachedProperty<Dependency<?>, Annotation> dependencyAnnotation = new AttachedProperty<>(
			"keyAnnotation");
	/**
	 * Used to place an annotation on a dependency without an injection point
	 */
	public static AttachedProperty<Dependency<?>, Class<?>> dependencyAnnotationClass = new AttachedProperty<>(
			"keyAnnotationClass");

	private Annotations() {
	}

	public static Matcher<Dependency<?>> matcher(
			Class<? extends Annotation> annotationType) {
		return d -> {
			// match annotation present at injection point
			if (d.injectionPoint != null
					&& d.injectionPoint.getAnnotated() != null
					&& d.injectionPoint.getAnnotated().isAnnotationPresent(
							annotationType))
				return true;
			// match annotationClass put on dependency
			if (annotationType.equals(Annotations.dependencyAnnotationClass
					.get(d)))
				return true;
			// match annotation put on dependency
			if (Annotations.dependencyAnnotation.get(d) != null
					&& annotationType.equals(Annotations.dependencyAnnotation
							.get(d).annotationType()))
				return true;
			return false;
		};
	}

	public static Matcher<Dependency<?>> matcher(Annotation annotation) {
		return d -> {
			// match annotation present at injection point
			if (d.injectionPoint != null
					&& d.injectionPoint.getAnnotated() != null) {
				for (Annotation a : d.injectionPoint.getAnnotated()
						.getAnnotations()) {
					if (annotation.equals(a))
						return true;
				}
			}
			// match annotation put on dependency
			if (annotation.equals(Annotations.dependencyAnnotation.get(d)))
				return true;

			// match annotationClass put on dependency
			if (Annotations.dependencyAnnotationClass.get(d) != null
					&& annotation.annotationType().equals(
							Annotations.dependencyAnnotationClass.get(d)
									.getClass()))
				return true;
			return false;
		};
	}
}

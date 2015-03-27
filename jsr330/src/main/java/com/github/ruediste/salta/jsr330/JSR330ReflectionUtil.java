package com.github.ruediste.salta.jsr330;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Named;
import javax.inject.Qualifier;

import com.github.ruediste.salta.jsr330.util.NamedImpl;

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

	public static Named createNamed(String name) {
		return new NamedImpl(name);
	}
}

package com.github.ruediste.simpledi;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.reflect.TypeToken;

/**
 * The type and required qualifiers to lookup an instance.
 */
public class Dependency<T> {
	public final TypeToken<T> type;
	public Set<Annotation> requiredQualifiers = new HashSet<>();

	InjectionPoint injectionPoint;

	public Dependency(Class<T> type) {
		this(TypeToken.of(type));
	}

	public static <T> Dependency<T> of(Class<T> type) {
		return new Dependency<>(type);
	}

	public Dependency(TypeToken<T> type) {
		this.type = type;
	}

	public static <T> Dependency<T> of(TypeToken<T> type) {
		return new Dependency<>(type);
	}

	@Override
	public String toString() {
		return "(" + type + "/" + requiredQualifiers + "/" + injectionPoint
				+ ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, requiredQualifiers, injectionPoint);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!getClass().equals(obj.getClass())) {
			return false;
		}

		Dependency<?> other = (Dependency<?>) obj;
		return Objects.equals(type, other.type)
				&& Objects.equals(requiredQualifiers, other.requiredQualifiers)
				&& Objects.equals(injectionPoint, other.injectionPoint);
	}
}

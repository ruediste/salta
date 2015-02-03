package com.github.ruediste.simpledi.core;

import java.util.Objects;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.google.common.reflect.TypeToken;

/**
 * The type and required qualifiers to lookup an instance.
 */
public class Dependency<T> extends AttachedPropertyBearerBase {
	public final TypeToken<T> type;

	/**
	 * Injection point to satisfy, can be null
	 */
	public final InjectionPoint injectionPoint;

	public Dependency(Class<T> type) {
		this(TypeToken.of(type));
	}

	public static <T> Dependency<T> of(Class<T> type) {
		return new Dependency<>(type);
	}

	public Dependency(TypeToken<T> type) {
		this.type = type;
		this.injectionPoint = null;
	}

	public static <T> Dependency<T> of(TypeToken<T> type) {
		return new Dependency<>(type);
	}

	@Override
	public String toString() {
		return "(" + type + "/" + injectionPoint + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, injectionPoint, getAttachedPropertyMap());
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
				&& Objects.equals(getAttachedPropertyMap(),
						other.getAttachedPropertyMap())
				&& Objects.equals(injectionPoint, other.injectionPoint);
	}
}

package com.github.ruediste.simpledi;

import java.util.Objects;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.google.common.reflect.TypeToken;

/**
 * The type and required qualifiers to lookup an instance.
 */
public class InstanceRequest<T> extends AttachedPropertyBearerBase {
	public final TypeToken<T> type;

	public InstanceRequest(InstanceRequest<T> other) {
		type = other.type;
		getAttachedPropertyMap().putAll(other);
	}

	public InstanceRequest(Class<T> type) {
		this(TypeToken.of(type));
	}

	public static <T> InstanceRequest<T> of(Class<T> type) {
		return new InstanceRequest<>(type);
	}

	public InstanceRequest(TypeToken<T> type) {
		this.type = type;
	}

	public static <T> InstanceRequest<T> of(TypeToken<T> type) {
		return new InstanceRequest<>(type);
	}

	@Override
	public String toString() {
		return "(type " + type + " " + getAttachedPropertyMap() + ")";
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

		InstanceRequest<?> other = (InstanceRequest<?>) obj;
		return Objects.equals(type, other.type)
				&& getAttachedPropertyMap().equals(
						other.getAttachedPropertyMap());
	}
}

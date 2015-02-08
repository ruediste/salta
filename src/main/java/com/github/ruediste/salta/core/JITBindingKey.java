package com.github.ruediste.salta.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Key created from the {@link CoreDependencyKey} using the
 * {@link JITBindingKeyRule}s
 */
public class JITBindingKey extends AttachedPropertyBearerBase {

	@Override
	public int hashCode() {
		return getAttachedPropertyMap().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!getClass().equals(obj.getClass()))
			return false;
		JITBindingKey other = (JITBindingKey) obj;
		return getAttachedPropertyMap().equals(other.getAttachedPropertyMap());
	}
}

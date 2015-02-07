package com.github.ruediste.salta.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

public class JitBindingKey extends AttachedPropertyBearerBase {

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
		JitBindingKey other = (JitBindingKey) obj;
		return getAttachedPropertyMap().equals(other.getAttachedPropertyMap());
	}
}

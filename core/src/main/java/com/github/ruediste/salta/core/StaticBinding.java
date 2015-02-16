package com.github.ruediste.salta.core;

import java.util.Set;

import com.google.common.reflect.TypeToken;

/**
 * Statically defined Binding.
 */
public abstract class StaticBinding extends Binding {
	/**
	 * If possible, return the types this binding matches. Used to optimize
	 * binding lookup. The binding will never be called, if
	 * {@link CoreDependencyKey#getType()} returns a type which is not in the
	 * returned set.
	 * 
	 * <p>
	 * If null is returned, {@link #matches(CoreDependencyKey)} will be called
	 * upon every lookup.
	 * </p>
	 */
	public Set<TypeToken<?>> getPossibleTypes() {
		return null;
	}

	public abstract boolean matches(CoreDependencyKey<?> dependency);
}

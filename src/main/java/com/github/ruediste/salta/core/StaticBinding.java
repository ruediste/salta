package com.github.ruediste.salta.core;

/**
 * Statically defined Binding.
 */
public interface StaticBinding extends Binding {
	boolean matches(Dependency<?> dependency);

}

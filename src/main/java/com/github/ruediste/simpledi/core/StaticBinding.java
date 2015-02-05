package com.github.ruediste.simpledi.core;

/**
 * Statically defined Binding.
 */
public interface StaticBinding extends Binding {
	boolean matches(Dependency<?> dependency);

}

package com.github.ruediste.simpledi.core;

import java.util.function.Supplier;

/**
 * A Scope defines a visibility for an instance. The scope can either reuse an
 * instance or decide to create a new instance.
 */
public interface Scope {
	public <T> T scope(Binding key, Supplier<T> unscoped);
}

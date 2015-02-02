package com.github.ruediste.simpledi;

public interface Injector {

	/**
	 * Create a new instance of the given class
	 */
	<T> T createInstance(Class<T> cls);

	<T> T createInstance(Dependency<T> key);

}

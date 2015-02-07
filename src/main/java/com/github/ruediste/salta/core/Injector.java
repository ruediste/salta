package com.github.ruediste.salta.core;

import com.google.common.reflect.TypeToken;

public interface Injector {

	/**
	 * Create a new instance of the given class
	 */
	<T> T createInstance(Class<T> cls);

	<T> T createInstance(Dependency<T> key);

	void injectMembers(Object instance);

	<T> void injectMembers(TypeToken<T> type, T instance);

}

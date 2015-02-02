package com.github.ruediste.simpledi;

import com.google.common.reflect.TypeToken;

/**
 * Rule to create an instantiator for a given type
 */
public interface InstantiatorRule {

	/**
	 * Create the instantiator or return null if the next rule should be tried.
	 */
	<T> Instantiator<T> apply(TypeToken<T> type);
}

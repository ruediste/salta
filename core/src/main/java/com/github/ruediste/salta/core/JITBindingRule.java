package com.github.ruediste.salta.core;

import com.github.ruediste.salta.standard.Injector;


/**
 * Rule used by the {@link Injector} if no {@link CreationRule} matches
 * and no binding is found to create a new binding.
 */
public interface JITBindingRule {

	JITBinding apply(JITBindingKey key);
}

package com.github.ruediste.salta.core;


/**
 * Rule used by the {@link Injector} if no {@link NoBindingInstanceCreationRule} matches
 * and no binding is found to create a new binding.
 */
public interface JITBindingRule {

	JITBinding apply(JitBindingKey key);
}

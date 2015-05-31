package com.github.ruediste.salta.core;

import com.github.ruediste.salta.standard.StandardInjector;

/**
 * Rule used by the {@link StandardInjector} if no {@link CreationRule} matches
 * and no binding is found to create a new binding.
 */
public interface JITBindingRule {

    JITBinding apply(JITBindingKey key);
}

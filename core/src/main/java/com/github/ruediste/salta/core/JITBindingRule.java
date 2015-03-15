package com.github.ruediste.salta.core;

import java.util.Optional;

import com.github.ruediste.salta.standard.Injector;

/**
 * Rule used by the {@link Injector} if no {@link CreationRule} matches and no
 * binding is found to create a new binding.
 */
public interface JITBindingRule {

	Optional<JITBinding> apply(JITBindingKey key);
}

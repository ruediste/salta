package com.github.ruediste.simpledi.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearer;

/**
 * Rule to create a key for a JIT Binding. All rules in
 * {@link InjectorConfiguration#jitBindingKeyRules} are applied to a key object
 * in order.
 */
public interface JITBindingKeyRule {

	void apply(Dependency<?> dependency, AttachedPropertyBearer key);
}

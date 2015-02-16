package com.github.ruediste.salta.core;

/**
 * Rule to create a key for a JIT Binding. All rules in
 * {@link CoreInjectorConfiguration#jitBindingKeyRules} are applied to a key
 * object in order.
 */
public interface JITBindingKeyRule {
	void apply(CoreDependencyKey<?> dependency, JITBindingKey key);
}

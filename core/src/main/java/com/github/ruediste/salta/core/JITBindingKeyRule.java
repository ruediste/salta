package com.github.ruediste.salta.core;

/**
 * Rule to create a key for a JIT Binding. All {@link JITBindingKeyRule}s are
 * applied to a key object in order.
 */
public interface JITBindingKeyRule {
    void apply(CoreDependencyKey<?> dependency, JITBindingKey key);
}

package com.github.ruediste.simpledi;

/**
 * Rule used by the {@link Injector} if no {@link InstanceCreationRule} matches
 * and no binding is found to create a new binding.
 */
public interface JITBindingRule {

	Binding apply(Dependency<?> dependency);
}

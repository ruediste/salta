package com.github.ruediste.simpledi.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Rule used by the {@link Injector} if no {@link InstanceCreationRule} matches
 * and no binding is found to create a new binding.
 */
public interface JITBindingRule {

	JITBinding apply(AttachedPropertyBearerBase key);
}

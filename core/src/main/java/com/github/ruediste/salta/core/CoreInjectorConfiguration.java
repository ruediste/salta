package com.github.ruediste.salta.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Contains the whole configuration of an injector.
 */
public class CoreInjectorConfiguration extends AttachedPropertyBearerBase {

	public InjectionStrategy injectionStrategy = InjectionStrategy.INVOKE_DYNAMIC;
}

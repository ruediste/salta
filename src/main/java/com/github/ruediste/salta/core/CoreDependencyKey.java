package com.github.ruediste.salta.core;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.google.common.reflect.TypeToken;

/**
 * Key to lookup a dependency.
 */
public abstract class CoreDependencyKey<T> extends AttachedPropertyBearerBase {
	/**
	 * Get the required type the looked up dependency
	 */
	public abstract TypeToken<T> getType();

	/**
	 * Get the {@link AnnotatedElement} representing the annotations affecting
	 * the lookup. Usually, this is just the {@link Field} or {@link Parameter}
	 * which is beeing injected. If dependencies are looked up directly from the
	 * injector, this can also be a synthetic implementation.
	 */
	public abstract AnnotatedElement getAnnotatedElement();
}

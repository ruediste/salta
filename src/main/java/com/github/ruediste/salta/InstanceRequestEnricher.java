package com.github.ruediste.salta;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.core.InjectionPoint;

/**
 * Enrich a {@link Dependency} based on an injection point. The goal is typically to
 * add additional {@link AttachedProperty}s to the key, such that they can be
 * used by the {@link Rule}s while creating a {@link CreationRecipe}.
 */
public interface InstanceRequestEnricher {
	void enrich(Dependency<?> request, InjectionPoint injectionPoint);
}

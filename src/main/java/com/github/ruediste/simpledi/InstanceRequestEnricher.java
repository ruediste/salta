package com.github.ruediste.simpledi;

import com.github.ruediste.attachedProperties4J.AttachedProperty;

/**
 * Enrich a {@link InstanceRequest} based on an injection point. The goal is typically to
 * add additional {@link AttachedProperty}s to the key, such that they can be
 * used by the {@link Rule}s while creating a {@link CreationRecipe}.
 */
public interface InstanceRequestEnricher {
	void enrich(InstanceRequest<?> request, InjectionPoint injectionPoint);
}

package com.github.ruediste.salta.core;

/**
 * Factory to create an {@link RecipeEnhancer} for a type
 */
public interface EnhancerFactory {

    /**
     * Create a {@link RecipeEnhancer} based on a type. If null is returned, the
     * result is ignored
     */
    RecipeEnhancer getEnhancer(RecipeCreationContext ctx, CoreDependencyKey<?> requestedKey);

}

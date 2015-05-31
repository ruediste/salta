package com.github.ruediste.salta.standard.config;

import java.util.List;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.recipe.RecipeInitializer;
import com.google.common.reflect.TypeToken;

/**
 * Factory to create a {@link RecipeInitializer} for a type. Used by
 * {@link StandardInjectorConfiguration#initializerFactories}
 */
public interface RecipeInitializerFactory {
    /**
     * Create a {@link RecipeInitializer}s based on a type. never return null.
     */
    List<RecipeInitializer> getInitializers(RecipeCreationContext ctx,
            TypeToken<?> type);
}

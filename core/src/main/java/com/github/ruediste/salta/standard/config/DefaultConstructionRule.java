package com.github.ruediste.salta.standard.config;

import java.util.Optional;
import java.util.function.Function;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration.ConstructionConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * Default {@link ConstructionRule}, using
 * {@link ConstructionConfiguration#createRecipeInstantiator(TypeToken)} and
 * {@link ConstructionConfiguration#createConstructionRecipe(RecipeCreationContext, TypeToken, com.github.ruediste.salta.standard.recipe.RecipeInstantiator)}
 * 
 * @author ruedi
 *
 */
public class DefaultConstructionRule implements ConstructionRule {

    private StandardInjectorConfiguration config;

    public DefaultConstructionRule(StandardInjectorConfiguration config) {
        this.config = config;
    }

    @Override
    public Optional<Function<RecipeCreationContext, SupplierRecipe>> createConstructionRecipe(
            TypeToken<?> type) {
        // create seed recipe
        return config.construction.createRecipeInstantiator(type).map(
                instantiator -> ctx -> config.construction
                        .createConstructionRecipe(ctx, type,
                                instantiator.apply(ctx)));

    }
}

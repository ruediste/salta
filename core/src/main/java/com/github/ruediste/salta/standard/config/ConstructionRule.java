package com.github.ruediste.salta.standard.config;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.google.common.reflect.TypeToken;

/**
 * Rule defining how to construct an instance, including instantiation, member
 * injection and initialization.
 *
 */
public interface ConstructionRule {

	SupplierRecipe createConstructionRecipe(RecipeCreationContext ctx,
			TypeToken<?> type);
}

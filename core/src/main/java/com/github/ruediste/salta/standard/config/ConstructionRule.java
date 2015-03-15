package com.github.ruediste.salta.standard.config;

import java.util.Optional;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.google.common.reflect.TypeToken;

/**
 * Rule defining how to construct an instance, including instantiation, member
 * injection and initialization. If {@link Optional#empty()} is returned, the
 * next rule is tried
 */
public interface ConstructionRule {

	Optional<SupplierRecipe> createConstructionRecipe(
			RecipeCreationContext ctx, TypeToken<?> type);
}

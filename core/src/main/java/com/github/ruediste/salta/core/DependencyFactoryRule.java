package com.github.ruediste.salta.core;

import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * When looking up a key, the {@link CoreInjector} first checks these rules. If
 * a matching rule is found, the recipe is used to create the dependency.
 */
public interface DependencyFactoryRule {

	SupplierRecipe apply(CoreDependencyKey<?> key, RecipeCreationContext ctx);
}

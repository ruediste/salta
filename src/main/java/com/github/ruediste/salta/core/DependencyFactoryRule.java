package com.github.ruediste.salta.core;

/**
 * When looking up a key, the {@link CoreInjector} first checks these rules (or
 * {@link DependencyFactory}s previously created). If a matching rule is found,
 * the recipe is used to create the dependency.
 */
public interface DependencyFactoryRule {

	CreationRecipe apply(CoreDependencyKey<?> key, RecipeCreationContext ctx);
}

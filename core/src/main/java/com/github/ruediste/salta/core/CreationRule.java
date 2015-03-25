package com.github.ruediste.salta.core;

import java.util.Optional;
import java.util.function.Function;

import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * When looking up a key, the {@link CoreInjector} first checks these rules. If
 * a matching rule is found, the recipe is used to create the dependency.
 * 
 * <p>
 * Unlike {@link Binding}s, creation rules can take the
 * {@link CoreDependencyKey} into account, providing them access to information
 * about the injection point. On the other hand, the created instances cannot be
 * {@link Scope scoped}
 * </p>
 */
public interface CreationRule {

	Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(
			CoreDependencyKey<?> key);

	static CreationRule combine(Iterable<CreationRule> rules) {
		return key -> {
			for (CreationRule rule : rules) {
				Optional<Function<RecipeCreationContext, SupplierRecipe>> result = rule
						.apply(key);
				if (result.isPresent())
					return result;
			}
			return Optional.empty();
		};
	}
}

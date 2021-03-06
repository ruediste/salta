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

    /**
     * Create a function to create the compileable recipe. The caller owns the
     * {@link CoreInjector#recipeLock}. This method should return as fast as
     * possible. The rule only needs to make sure if it really matches the given
     * injection point. Any expensive operations should be performed while the
     * returned function is executed. It is guaranteed that it is only invoked
     * once (while the caller owns the recipe lock.
     */
    Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(CoreDependencyKey<?> key, CoreInjector injector);

    static CreationRule combine(Iterable<CreationRule> rules) {
        return (key, injector) -> {
            for (CreationRule rule : rules) {
                Optional<Function<RecipeCreationContext, SupplierRecipe>> result = rule.apply(key, injector);
                if (result.isPresent())
                    return result;
            }
            return Optional.empty();
        };
    }
}

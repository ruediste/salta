package com.github.ruediste.salta.core;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public interface RecipeCreationContext {

    SupplierRecipe getRecipe(CoreDependencyKey<?> dependency);

    RecipeCompiler getCompiler();

    void queueAction(Runnable action);

    /**
     * get {@link CoreInjector#recipeLock}
     */
    Object getRecipeLock();

    <T> T withBinding(Binding binding, Supplier<T> supplier);

    Optional<SupplierRecipe> tryGetRecipe(CoreDependencyKey<?> dependency);

    Optional<Function<RecipeCreationContext, SupplierRecipe>> tryGetRecipeFunc(CoreDependencyKey<?> dep);
}
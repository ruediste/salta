package com.github.ruediste.salta.core;

import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * A Scope defines a visibility for an instance. The scope can either reuse an
 * instance or decide to create a new instance.
 */
public interface Scope {

    /**
     * Create a recipe. No incoming parameter is on the stack. The scoped
     * instance is expected afterwards. The calling thread always holds the
     * {@link CoreInjector#recipeLock}
     * 
     * @param binding
     *            binding which is beeing scoped
     * @param requestedKey
     *            the key beeing requested
     */
    SupplierRecipe createRecipe(RecipeCreationContext ctx, Binding binding,
            CoreDependencyKey<?> requestedKey);

    /**
     * Perform an eager instantiation if applicable for this scope. Only called
     * if eager instantiations should actually be perfomed, so the scope does
     * not have to check a configuration by itself.
     */
    default void performEagerInstantiation(RecipeCreationContext ctx,
            Binding binding) {
    }

}

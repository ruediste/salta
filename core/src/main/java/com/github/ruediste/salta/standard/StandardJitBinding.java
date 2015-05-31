package com.github.ruediste.salta.standard;

import java.util.function.Supplier;

import com.github.ruediste.salta.core.JITBinding;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.google.common.reflect.TypeToken;

/**
 * Statically defined Binding.
 */
public class StandardJitBinding extends JITBinding {

    public CreationRecipeFactory recipeFactory;
    public Supplier<Scope> scopeSupplier;

    private TypeToken<?> type;

    public StandardJitBinding(TypeToken<?> type) {
        this.type = type;
    }

    @Override
    public SupplierRecipe createRecipe(RecipeCreationContext ctx) {
        return recipeFactory.createRecipe(ctx);
    }

    @Override
    public String toString() {
        return "StandardJitBinding(" + type + ")";
    }

    @Override
    protected Scope getScopeImpl() {
        return scopeSupplier.get();
    }
}

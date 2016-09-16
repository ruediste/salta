package com.github.ruediste.salta.standard;

import java.util.Optional;
import java.util.function.Function;

import com.github.ruediste.salta.core.JITBinding;
import com.github.ruediste.salta.core.JITBindingKey;
import com.github.ruediste.salta.core.JITBindingRule;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

public final class DefaultJITBindingRule implements JITBindingRule {
    private StandardInjectorConfiguration config;

    public DefaultJITBindingRule(StandardInjectorConfiguration config) {
        this.config = config;
    }

    @Override
    public JITBinding apply(JITBindingKey key) {
        TypeToken<?> type = DefaultJITBindingKeyRule.jitBindingKeyType.get(key);
        if (!config.doQualifiersMatch(DefaultJITBindingKeyRule.jitBindingKeyRequiredQualifiers.get(key),
                config.getAvailableQualifier(type.getRawType())))
            return null;

        Optional<Function<RecipeCreationContext, SupplierRecipe>> recipe = config.construction
                .createConstructionRecipe(type).map(seed -> ctx -> seed.apply(ctx));
        if (!recipe.isPresent())
            return null;

        StandardJitBinding binding = new StandardJitBinding(type);
        binding.recipeFactory = recipe.get()::apply;
        binding.scopeSupplier = () -> config.scope.getScope(type);
        return binding;
    }
}
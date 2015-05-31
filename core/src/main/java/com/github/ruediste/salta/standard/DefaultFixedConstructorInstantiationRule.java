package com.github.ruediste.salta.standard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Optional;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.standard.config.FixedConstructorInstantiationRule;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.FixedConstructorRecipeInstantiator;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.base.Defaults;
import com.google.common.reflect.TypeToken;

/**
 * Default {@link FixedConstructorInstantiationRule}, using
 * {@link StandardInjectorConfiguration#isInjectionOptional(java.lang.reflect.AnnotatedElement)}
 */
public class DefaultFixedConstructorInstantiationRule implements
        FixedConstructorInstantiationRule {

    private StandardInjectorConfiguration config;

    public DefaultFixedConstructorInstantiationRule(
            StandardInjectorConfiguration config) {
        this.config = config;
    }

    @Override
    public Optional<RecipeInstantiator> create(TypeToken<?> typeToken,
            RecipeCreationContext ctx, Constructor<?> constructor) {

        ArrayList<SupplierRecipe> args = resolveArguments(config, typeToken,
                ctx, constructor);
        return Optional.of(createRecipeInstantiator(typeToken, constructor,
                args));

    }

    public static ArrayList<SupplierRecipe> resolveArguments(
            StandardInjectorConfiguration config, TypeToken<?> typeToken,
            RecipeCreationContext ctx, Constructor<?> constructor) {
        ArrayList<SupplierRecipe> args = new ArrayList<>();

        Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            @SuppressWarnings({ "unchecked", "rawtypes" })
            CoreDependencyKey<Object> dependency = new InjectionPoint(
                    typeToken.resolveType(parameter.getParameterizedType()),
                    constructor, parameter, i);
            Optional<SupplierRecipe> argRecipe = ctx.tryGetRecipe(dependency);
            if (argRecipe.isPresent())
                args.add(argRecipe.get());
            else {
                if (config.isInjectionOptional(parameter)) {
                    args.add(new SupplierRecipeImpl(() -> Defaults
                            .defaultValue(parameter.getType())));
                } else {
                    throw new SaltaException(
                            "Cannot resolve constructor parameter of "
                                    + constructor + ":\n" + parameter);
                }
            }
        }
        return args;
    }

    protected FixedConstructorRecipeInstantiator createRecipeInstantiator(
            TypeToken<?> typeToken, Constructor<?> constructor,
            ArrayList<SupplierRecipe> args) {
        return new FixedConstructorRecipeInstantiator(constructor, args);
    }
}

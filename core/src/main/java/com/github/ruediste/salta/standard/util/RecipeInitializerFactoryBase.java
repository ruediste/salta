package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.config.RecipeInitializerFactory;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.FixedMethodRecipeInitializer;
import com.github.ruediste.salta.standard.recipe.RecipeInitializer;
import com.google.common.base.Defaults;
import com.google.common.reflect.TypeToken;

public abstract class RecipeInitializerFactoryBase implements
        RecipeInitializerFactory {

    private StandardInjectorConfiguration config;

    public RecipeInitializerFactoryBase(StandardInjectorConfiguration config) {
        this.config = config;

    }

    @Override
    public List<RecipeInitializer> getInitializers(RecipeCreationContext ctx,
            TypeToken<?> typeToken) {
        ArrayList<RecipeInitializer> result = new ArrayList<>();

        MethodOverrideIndex overrideIndex = new MethodOverrideIndex(typeToken);
        // iterate over super types, always processing supertypes before
        // subtypes
        for (TypeToken<?> t : overrideIndex.getAncestors()) {

            for (Method method : t.getRawType().getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers())
                        || Modifier.isAbstract(method.getModifiers()))
                    continue;
                if (isInitializer(t, method, overrideIndex)) {
                    method.setAccessible(true);

                    // create dependencies
                    ArrayList<SupplierRecipe> args = new ArrayList<>();
                    Parameter[] parameters = method.getParameters();
                    for (int i = 0; i < parameters.length; i++) {
                        Parameter parameter = parameters[i];
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        CoreDependencyKey<Object> dependency = new InjectionPoint<>(
                                (TypeToken) t.resolveType(parameter
                                        .getParameterizedType()), method,
                                parameter, i);
                        Optional<SupplierRecipe> recipe = ctx
                                .tryGetRecipe(dependency);

                        if (recipe.isPresent())
                            args.add(recipe.get());
                        else if (config.isInjectionOptional(parameter))
                            args.add(new SupplierRecipeImpl(() -> Defaults
                                    .defaultValue(parameter.getType())));
                        else
                            throw new SaltaException(
                                    "Cannot resolve initializer parameter of "
                                            + method + ":\n" + parameter);

                    }

                    // add injector
                    result.add(new FixedMethodRecipeInitializer(method, args));
                }
            }
        }
        return result;
    }

    protected abstract boolean isInitializer(TypeToken<?> declaringType,
            Method method, MethodOverrideIndex overrideIndex);

}

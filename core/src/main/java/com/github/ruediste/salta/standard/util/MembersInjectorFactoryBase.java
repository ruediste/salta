package com.github.ruediste.salta.standard.util;

import java.lang.reflect.Field;
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
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.FixedFieldRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.FixedMethodRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjectorFactory;
import com.google.common.base.Defaults;
import com.google.common.reflect.TypeToken;

public abstract class MembersInjectorFactoryBase implements
        RecipeMembersInjectorFactory {

    private StandardInjectorConfiguration config;

    public MembersInjectorFactoryBase(StandardInjectorConfiguration config) {
        this.config = config;
    }

    protected enum InjectionInstruction {
        NO_INJECTION, INJECT, INJECT_OPTIONAL
    }

    @Override
    public List<RecipeMembersInjector> createMembersInjectors(
            RecipeCreationContext ctx, TypeToken<?> typeToken) {
        ArrayList<RecipeMembersInjector> result = new ArrayList<>();

        MethodOverrideIndex overrideIndex = new MethodOverrideIndex(typeToken);
        // iterate over super types, always processing supertypes before
        // subtypes
        for (TypeToken<?> t : overrideIndex.getAncestors()) {
            for (Field f : t.getRawType().getDeclaredFields()) {
                InjectionInstruction injectionInstruction = getInjectionInstruction(
                        t, f);
                if (injectionInstruction != InjectionInstruction.NO_INJECTION) {
                    f.setAccessible(true);

                    // create dependency
                    CoreDependencyKey<?> dependency = new InjectionPoint<>(
                            t.resolveType(f.getGenericType()), f, f, null);

                    // add injector
                    Optional<SupplierRecipe> recipe;
                    recipe = ctx.tryGetRecipe(dependency);
                    if (recipe.isPresent()) {
                        result.add(new FixedFieldRecipeMembersInjector(f,
                                recipe.get()));
                    } else {
                        if (injectionInstruction != InjectionInstruction.INJECT_OPTIONAL)
                            throw new SaltaException(
                                    "No recipe found for field\n" + f);
                    }

                }
            }

            methodLoop: for (Method method : t.getRawType()
                    .getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()))
                    continue;
                InjectionInstruction injectionInstruction = getInjectionInstruction(
                        t, method, overrideIndex);
                if (injectionInstruction != InjectionInstruction.NO_INJECTION) {
                    method.setAccessible(true);

                    // check that no qualifiers are given on the method itself
                    if (config.getRequiredQualifier(method, method) != null) {
                        /*
                         * In Scala, fields automatically get accessor methods
                         * with the same name. So we don't do misplaced-binding
                         * annotation detection if the offending method has a
                         * matching field.
                         */
                        boolean fieldFound = false;
                        for (Field f : method.getDeclaringClass()
                                .getDeclaredFields()) {
                            if (f.getName().equals(method.getName())) {
                                fieldFound = true;
                                break;
                            }
                        }
                        if (!fieldFound)
                            throw new SaltaException(
                                    "Qualifier has been specified on "
                                            + method
                                            + ".\nSpecify qualifiers on parameters instead");
                    }

                    // create dependencies
                    ArrayList<SupplierRecipe> args = new ArrayList<>();
                    Parameter[] parameters = method.getParameters();
                    parameterLoop: for (int i = 0; i < parameters.length; i++) {
                        Parameter parameter = parameters[i];
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        CoreDependencyKey<Object> dependency = new InjectionPoint<>(
                                (TypeToken) t.resolveType(parameter
                                        .getParameterizedType()), method,
                                parameter, i);
                        Optional<SupplierRecipe> recipe;
                        recipe = ctx.tryGetRecipe(dependency);
                        if (!recipe.isPresent()) {
                            if (config.isInjectionOptional(parameter)) {
                                args.add(new SupplierRecipeImpl(() -> Defaults
                                        .defaultValue(parameter.getType())));
                                continue parameterLoop;
                            }
                            if (injectionInstruction == InjectionInstruction.INJECT_OPTIONAL) {
                                continue methodLoop;
                            }
                        }

                        args.add(recipe.orElseThrow(() -> new SaltaException(
                                "Unable to get recipe for parameter of method "
                                        + method + ":\n" + parameter)));
                    }

                    // add injector
                    result.add(new FixedMethodRecipeMembersInjector(method,
                            args));
                }
            }
        }
        return result;
    }

    protected abstract InjectionInstruction getInjectionInstruction(
            TypeToken<?> declaringType, Field field);

    protected abstract InjectionInstruction getInjectionInstruction(
            TypeToken<?> declaringType, Method method,
            MethodOverrideIndex overrideIndex);

}

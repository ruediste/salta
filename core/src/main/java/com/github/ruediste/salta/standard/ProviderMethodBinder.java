package com.github.ruediste.salta.standard;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.StaticBinding;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.FixedMethodInvocationFunctionRecipe;
import com.github.ruediste.salta.standard.util.MethodOverrideIndex;
import com.google.common.reflect.TypeToken;

/**
 * Scans an instance for methods which are used to create instances and binds
 * them.
 */
public abstract class ProviderMethodBinder {
    private StandardInjectorConfiguration config;

    public ProviderMethodBinder(StandardInjectorConfiguration config) {
        this.config = config;
    }

    public void bindProviderMethodsOf(Object instance) {
        MethodOverrideIndex index = new MethodOverrideIndex(TypeToken.of(instance.getClass()));
        for (TypeToken<?> t : index.getAncestors()) {
            Class<?> rawType = t.getRawType();
            if (rawType.isInterface())
                continue;

            for (Method m : rawType.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers()) || m.isBridge() || m.isSynthetic()) {
                    continue;
                }
                if (index.isOverridden(m))
                    continue;

                if (!isProviderMethod(m)) {
                    continue;
                }
                m.setAccessible(true);

                TypeToken<?> boundType = t.resolveType(m.getGenericReturnType());
                Matcher<CoreDependencyKey<?>> matcher = CoreDependencyKey.typeMatcher(boundType)
                        .and(config.requredQualifierMatcher(config.getAvailableQualifier(m)));

                config.creationPipeline.staticBindings.add(new StaticBinding() {

                    @Override
                    protected SupplierRecipe createRecipe(RecipeCreationContext ctx) {
                        ArrayList<SupplierRecipe> args = new ArrayList<>();
                        Parameter[] parameters = m.getParameters();
                        for (int i = 0; i < parameters.length; i++) {
                            Parameter p = parameters[i];
                            args.add(ctx
                                    .getRecipe(new InjectionPoint<>(t.resolveType(p.getParameterizedType()), m, p, i)));
                        }
                        FixedMethodInvocationFunctionRecipe methodRecipe = new FixedMethodInvocationFunctionRecipe(m,
                                args);
                        return new SupplierRecipe() {

                            @Override
                            protected Class<?> compileImpl(GeneratorAdapter mv, MethodCompilationContext ctx) {
                                ctx.addFieldAndLoad(Object.class, instance);
                                return methodRecipe.compile(Object.class, ctx);
                            }
                        };
                    }

                    @Override
                    public String toString() {
                        return "ProviderMethodBinding " + m;
                    }

                    @Override
                    public Matcher<CoreDependencyKey<?>> getMatcher() {
                        return matcher;
                    }

                    @Override
                    protected Scope getScopeImpl() {
                        return config.scope.getScope(m);
                    }
                });
            }
        }
    }

    protected abstract boolean isProviderMethod(Method m);
}

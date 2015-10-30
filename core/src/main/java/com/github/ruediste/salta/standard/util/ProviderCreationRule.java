package com.github.ruediste.salta.standard.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.google.common.reflect.TypeToken;

/**
 * Base class for {@link CreationRule}s which match some sort of Provider (as in
 * javax.inject.Provider).
 * 
 * <p>
 * The initialization of providers is quite delicate. This class makes
 * implementing such factories simple. Just provide a matcher which matches the
 * injection points you want to inject a provider into and a wrapper, which
 * creates an instance of the correct class, delegating to a supplied
 * {@link Supplier}.
 * </p>
 */
public class ProviderCreationRule implements CreationRule {

    private Matcher<? super CoreDependencyKey<?>> matcher;
    private BiFunction<CoreDependencyKey<?>, Supplier<?>, Object> wrapper;
    private Class<?> providerType;

    public static class ProviderAccessBeforeInstanceCreationFinishedException
            extends SaltaException {
        private static final long serialVersionUID = 1L;

        ProviderAccessBeforeInstanceCreationFinishedException() {
            super("Attempt to access injected Provider before the instance construction finished (e.g. from construction, injected method or post construct method)");
        }
    }

    protected static class ProviderImpl implements Supplier<Object> {
        private volatile CompiledSupplier compiledRecipe;

        private ThreadLocal<Boolean> isGetting = new ThreadLocal<>();

        private CoreDependencyKey<?> dependency;

        private Object recipeLock;

        private Supplier<CompiledSupplier> compiledRecipeSupplier;

        public ProviderImpl(CoreDependencyKey<?> dependency, Object recipeLock,
                Supplier<CompiledSupplier> compiledRecipeSupplier) {
            this.dependency = dependency;
            this.recipeLock = recipeLock;
            this.compiledRecipeSupplier = compiledRecipeSupplier;
        }

        @Override
        public Object get() {
            if (isGetting.get() != null)
                throw new ProviderAccessBeforeInstanceCreationFinishedException();
            isGetting.set(true);

            try {
                if (compiledRecipe == null) {
                    synchronized (recipeLock) {
                        if (compiledRecipe == null) {
                            compiledRecipe = compiledRecipeSupplier.get();
                        }
                    }
                }

                return compiledRecipe.get();
            } catch (SaltaException e) {
                throw e;
            } catch (Throwable e) {
                throw new SaltaException(
                        "Error while getting instance from provider for key "
                                + dependency,
                        e);
            } finally {
                isGetting.remove();
            }
        }

        @Override
        public String toString() {
            return "Provider<" + dependency + ">";
        }
    }

    /**
     * Create a new instance
     * 
     * @param matcher
     *            matcher for the injection points which should be injected with
     *            the provider
     * @param wrapper
     *            wrapper of a supplier to the actual instance which gets
     *            injected
     */
    public ProviderCreationRule(Matcher<? super CoreDependencyKey<?>> matcher,
            BiFunction<CoreDependencyKey<?>, Supplier<?>, Object> wrapper,
            Class<?> providerType) {
        this.matcher = matcher;
        this.wrapper = wrapper;
        this.providerType = providerType;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(
            CoreDependencyKey<?> key, CoreInjector injector) {

        if (matcher.matches(key)) {
            if (key.getType().getType() instanceof Class) {
                throw new SaltaException(
                        "Cannot inject a Provider that has no type parameter");
            }
            // determine dependency
            TypeToken<?> requestedType = key.getType()
                    .resolveType(providerType.getTypeParameters()[0]);

            CoreDependencyKey<?> dep;
            if (key instanceof InjectionPoint) {
                InjectionPoint p = (InjectionPoint) key;
                dep = new InjectionPoint(requestedType, p.getMember(),
                        p.getAnnotatedElement(), p.getParameterIndex());

            } else {
                dep = DependencyKey.of(requestedType).withAnnotations(
                        key.getAnnotatedElement().getAnnotations());
            }
            Optional<Function<RecipeCreationContext, SupplierRecipe>> innerRecipe = injector
                    .tryGetRecipeFunc(dep);

            if (!innerRecipe.isPresent())
                return Optional.empty();

            return Optional
                    .of(new Function<RecipeCreationContext, SupplierRecipe>() {
                        @Override
                        public String toString() {
                            return "Provider[" + key + "]";
                        }

                        @Override
                        public SupplierRecipe apply(RecipeCreationContext ctx) {

                            // create and wrap provider instance
                            RecipeCompiler compiler = ctx.getCompiler();
                            ProviderImpl provider = new ProviderImpl(key,
                                    ctx.getRecipeLock(),
                                    () -> compiler.compileSupplier(
                                            innerRecipe.get().apply(ctx)));

                            Object wrappedProvider = wrapper.apply(key,
                                    provider);

                            // create creation recipe
                            SupplierRecipe creationRecipe = new SupplierRecipe() {

                                @Override
                                protected Class<?> compileImpl(
                                        GeneratorAdapter mv,
                                        MethodCompilationContext ctx) {
                                    ctx.addFieldAndLoad((Class) providerType,
                                            wrappedProvider);
                                    return providerType;
                                }
                            };

                            return creationRecipe;
                        }
                    });
        }

        return Optional.empty();
    }
}

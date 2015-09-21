package com.github.ruediste.salta.core;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.compile.FunctionRecipe;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

public class CoreInjector {
    /**
     * Lock object for recipe creation and compilation.
     */
    public final Object recipeLock = new Object();

    @SuppressWarnings("unused")
    private CoreInjectorConfiguration config;

    private final RecipeCompiler compiler;

    private ConcurrentHashMap<CoreDependencyKey<?>, Optional<CompiledSupplier>> compiledRecipeCache = new ConcurrentHashMap<>();

    private HashMap<CoreDependencyKey<?>, Optional<Function<RecipeCreationContext, SupplierRecipe>>> recipeCache = new HashMap<>();

    private List<CreationRule> creationRules;

    /**
     * Create and initialize this injector
     */
    public CoreInjector(CoreInjectorConfiguration config,
            List<CreationRule> creationRules) {
        this.config = config;
        this.creationRules = creationRules;
        compiler = new RecipeCompiler(config.generatedCodeParentClassLoader);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(CoreDependencyKey<T> key) {
        Optional<CompiledSupplier> tryGetCompiledRecipe;
        try {
            tryGetCompiledRecipe = tryGetCompiledRecipe(key);
        } catch (Throwable e) {
            throw new SaltaException(
                    "Error while creating instance for " + key, e);
        }

        if (!tryGetCompiledRecipe.isPresent()) {
            Class<T> rawType = key.getRawType();
            if (rawType.getEnclosingClass() != null)
                throw new SaltaException("No instance found for inner class "
                        + key + ".\nForgotten to make inner class static?");
            throw new SaltaException("Cannot create instance for " + key);
        }
        try {
            return (T) tryGetCompiledRecipe.get().get();
        } catch (Throwable e) {
            throw new SaltaException(
                    "Error while creating instance for " + key, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Supplier<T> getInstanceSupplier(CoreDependencyKey<T> key) {
        CompiledSupplier compiledRecipe = getCompiledRecipe(key);
        return () -> {
            try {
                return (T) compiledRecipe.get();
            } catch (SaltaException e) {
                throw e;
            } catch (Throwable e) {
                throw new SaltaException("Error while creating instance for "
                        + key, e);
            }
        };
    }

    public CompiledSupplier getCompiledRecipe(CoreDependencyKey<?> key) {
        Optional<CompiledSupplier> tryGetCompiledRecipe = tryGetCompiledRecipe(key);
        if (!tryGetCompiledRecipe.isPresent())
            throw new SaltaException("No recipe found for " + key);
        return tryGetCompiledRecipe.get();
    }

    public Optional<CompiledSupplier> tryGetCompiledRecipe(
            CoreDependencyKey<?> key) {
        // use Double Checked Locking
        Optional<CompiledSupplier> compiledRecipe = compiledRecipeCache
                .get(key);
        if (compiledRecipe != null)
            return compiledRecipe;

        synchronized (recipeLock) {
            compiledRecipe = compiledRecipeCache.get(key);
            if (compiledRecipe == null) {
                compiledRecipe = tryGetRecipe(key).map(x -> compileSupplier(x));
                compiledRecipeCache.put(key, compiledRecipe);
            }
            return compiledRecipe;
        }
    }

    public CompiledSupplier compileSupplier(SupplierRecipe recipe) {
        synchronized (recipeLock) {
            return compiler.compileSupplier(recipe);
        }
    }

    public CompiledFunction compileFunction(FunctionRecipe recipe) {
        synchronized (recipeLock) {
            return compiler.compileFunction(recipe);
        }
    }

    public SupplierRecipe getRecipe(CoreDependencyKey<?> key) {
        return getFromOptional(tryGetRecipe(key), key);
    }

    public <T> T withRecipeCreationContext(
            Function<RecipeCreationContext, T> func) {
        RecipeCreationContextImpl ctx = new RecipeCreationContextImpl(
                CoreInjector.this);
        T result = func.apply(ctx);
        try {
            ctx.processQueuedActions();
        } catch (SaltaException e) {
            throw new SaltaException("Error while processing queued actions", e);
        }
        return result;
    }

    public SupplierRecipe getRecipe(CoreDependencyKey<?> key,
            RecipeCreationContext ctx) {
        Function<RecipeCreationContext, SupplierRecipe> recipeFunction = getFromOptional(
                tryGetRecipeFunc(key), key);
        SupplierRecipe result = recipeFunction.apply(ctx);
        if (result == null)
            throw new SaltaException("Error while creating recipe for " + key
                    + ". The result of " + recipeFunction
                    + " was null, which should not happen");
        return result;
    }

    /**
     * Get the value of the optional, throwing an exception including the key if
     * the optional is empty
     */
    private <T> T getFromOptional(Optional<T> optional, CoreDependencyKey<?> key) {
        if (optional.isPresent())
            return optional.get();
        throw new SaltaException("Dependency cannot be resolved:\n" + key);
    }

    public Optional<SupplierRecipe> tryGetRecipe(CoreDependencyKey<?> key) {
        return withRecipeCreationContext(ctx -> tryGetRecipeFunc(key).map(
                f -> f.apply(ctx)));
    }

    /**
     * Get the recipe for a key
     */
    public Optional<Function<RecipeCreationContext, SupplierRecipe>> tryGetRecipeFunc(
            CoreDependencyKey<?> key) {
        // acquire the recipe lock
        synchronized (recipeLock) {
            Optional<Function<RecipeCreationContext, SupplierRecipe>> result = recipeCache
                    .get(key);
            if (result == null) {
                try {
                    result = createRecipe(key);
                    recipeCache.put(key, result);
                } catch (Throwable t) {
                    recipeCache.remove(key);
                    throw new SaltaException("Error while creating recipe for "
                            + key, t);
                }
            }
            return result;
        }
    }

    /**
     * Create a recipe. Expects the calling thread to own the
     * {@link #recipeLock}
     */
    private Optional<Function<RecipeCreationContext, SupplierRecipe>> createRecipe(
            CoreDependencyKey<?> key) {
        try {
            Optional<Function<RecipeCreationContext, SupplierRecipe>> recipeFunction = Optional
                    .empty();
            // check rules
            for (CreationRule rule : creationRules) {
                recipeFunction = rule.apply(key, this);
                if (recipeFunction.isPresent())
                    break;
            }

            return recipeFunction
                    .map(new Function<Function<RecipeCreationContext, SupplierRecipe>, Function<RecipeCreationContext, SupplierRecipe>>() {
                        @Override
                        public Function<RecipeCreationContext, SupplierRecipe> apply(
                                Function<RecipeCreationContext, SupplierRecipe> f) {
                            return new Function<RecipeCreationContext, SupplierRecipe>() {
                                @Override
                                public SupplierRecipe apply(
                                        RecipeCreationContext ctx) {
                                    return config.applyEnhancers(f.apply(ctx),
                                            ctx, key);
                                }

                                @Override
                                public String toString() {
                                    return "apply enhancers[" + f + "]";
                                }
                            };
                        }
                    });
        } catch (SaltaException e) {
            throw e;
        } catch (Exception e) {
            throw new SaltaException("Error creating recipe for " + key, e);
        }
    }

    public RecipeCompiler getCompiler() {
        return compiler;
    }

}

package com.github.ruediste.salta.core;

import java.util.HashMap;
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

	private CoreInjectorConfiguration config;

	private final RecipeCompiler compiler = new RecipeCompiler();

	private ConcurrentHashMap<CoreDependencyKey<?>, Optional<CompiledSupplier>> compiledRecipeCache = new ConcurrentHashMap<>();

	private HashMap<CoreDependencyKey<?>, Optional<Function<RecipeCreationContext, SupplierRecipe>>> recipeCache = new HashMap<>();

	private HashMap<JITBindingKey, JITBinding> jitBindings = new HashMap<>();

	private StaticBindingSet staticBindings;
	private StaticBindingSet automaticStaticBindings;

	/**
	 * Create and initialize this injector
	 */
	public CoreInjector(CoreInjectorConfiguration config) {
		this.config = config;

		staticBindings = new StaticBindingSet(config.staticBindings);
		automaticStaticBindings = new StaticBindingSet(
				config.automaticStaticBindings);
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
			throw new SaltaException("No instance found for " + key);
		}
		return (T) tryGetCompiledRecipe.get().getNoThrow();
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
		return getFromOptional(tryGetRecipeFunc(key), key).apply(ctx);
	}

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
			// check rules
			for (CreationRule rule : config.creationRules) {
				Function<RecipeCreationContext, SupplierRecipe> recipe = rule
						.apply(key);
				if (recipe != null)
					return Optional.of(recipe);
			}

			// check static bindings
			{
				StaticBinding binding = staticBindings.getBinding(key);
				if (binding != null) {
					return Optional.of(binding.getScope().createRecipe(binding,
							key.getType(), binding.getOrCreateRecipe()));
				}
			}

			// check automatic static bindings
			{
				StaticBinding binding = automaticStaticBindings.getBinding(key);
				if (binding != null) {
					return Optional.of(binding.getScope().createRecipe(binding,
							key.getType(), binding.getOrCreateRecipe()));
				}
			}

			// create JIT binding
			{
				// create key
				JITBindingKey jitKey = new JITBindingKey();
				for (JITBindingKeyRule rule : config.jitBindingKeyRules) {
					rule.apply(key, jitKey);
				}

				// check existing bindings and create new one if necessary
				JITBinding jitBinding;
				jitBinding = jitBindings.get(jitKey);
				if (jitBinding == null) {
					for (JITBindingRule rule : config.jitBindingRules) {
						jitBinding = rule.apply(jitKey);
						if (jitBinding != null) {
							jitBindings.put(jitKey, jitBinding);
							break;
						}
					}
				}

				// use binding if available
				if (jitBinding != null) {
					JITBinding tmp = jitBinding;
					return Optional.of(tmp.getScope().createRecipe(tmp,
							key.getType(), tmp.getOrCreateRecipe()));
				}
			}
		} catch (SaltaException e) {
			throw e;
		} catch (Exception e) {
			throw new SaltaException("Error creating recipe for " + key, e);
		}
		return Optional.empty();
	}

	public RecipeCompiler getCompiler() {
		return compiler;
	}

}

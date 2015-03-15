package com.github.ruediste.salta.core;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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

	private ConcurrentHashMap<CoreDependencyKey<?>, CompiledSupplier> compiledRecipeCache = new ConcurrentHashMap<>();

	private HashMap<CoreDependencyKey<?>, Optional<SupplierRecipe>> recipeCache = new HashMap<>();

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
		try {
			return (T) getCompiledRecipe(key).get();
		} catch (SaltaException e) {
			throw e;
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
		// use Double Checked Locking
		CompiledSupplier compiledRecipe = compiledRecipeCache.get(key);
		if (compiledRecipe != null)
			return compiledRecipe;

		synchronized (recipeLock) {
			compiledRecipe = compiledRecipeCache.get(key);
			if (compiledRecipe == null) {
				compiledRecipe = compileSupplier(getRecipe(key));
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
		synchronized (recipeLock) {
			RecipeCreationContextImpl ctx = new RecipeCreationContextImpl(
					CoreInjector.this);

			SupplierRecipe result = getRecipe(key, ctx);

			try {
				ctx.processQueuedActions();
			} catch (SaltaException e) {
				throw new SaltaException(
						"Error while processing queued actions for " + key, e);
			}

			return result;
		}
	}

	/**
	 * Get the recipe for a key
	 */
	public SupplierRecipe getRecipe(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		Optional<SupplierRecipe> result = tryGetRecipe(key, ctx);
		if (!result.isPresent()) {
			throw new SaltaException("Dependency cannot be resolved:\n" + key);
		}
		return result.get();
	}

	/**
	 * Get the recipe for a key
	 */
	public Optional<SupplierRecipe> tryGetRecipe(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		// acquire the recipe lock
		synchronized (recipeLock) {
			Optional<SupplierRecipe> result = recipeCache.get(key);
			if (result == null) {
				try {
					result = createRecipe(key, ctx);
				} catch (Exception e) {
					throw new SaltaException("Error while creating recipe for "
							+ key, e);
				}
				recipeCache.put(key, result);
			}
			return result;
		}
	}

	/**
	 * Create a recipe. Expects the {@link #recipeLock} to be acquired
	 */
	private Optional<SupplierRecipe> createRecipe(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		try {
			// check rules
			for (CreationRule rule : config.creationRules) {
				SupplierRecipe recipe = rule.apply(key, ctx);
				if (recipe != null)
					return Optional.of(recipe);
			}

			// check static bindings
			{
				StaticBinding binding = staticBindings.getBinding(key);
				if (binding != null) {
					return Optional.of(binding.getScope().createRecipe(ctx,
							binding, key.getType(),
							ctx.getOrCreateRecipe(binding)));
				}
			}

			// check automatic static bindings
			{
				StaticBinding binding = automaticStaticBindings.getBinding(key);
				if (binding != null) {
					return Optional.of(binding.getScope().createRecipe(ctx,
							binding, key.getType(),
							ctx.getOrCreateRecipe(binding)));
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
					return Optional.of(jitBinding.getScope().createRecipe(ctx,
							jitBinding, key.getType(),
							ctx.getOrCreateRecipe(jitBinding)));
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

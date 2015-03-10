package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.compile.FunctionRecipe;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.matchers.Matcher;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;

public class CoreInjector {

	/**
	 * Lock object for recipe creation and compilation.
	 */
	public final Object recipeLock = new Object();

	private CoreInjectorConfiguration config;

	private final RecipeCompiler compiler = new RecipeCompiler();

	private ConcurrentHashMap<CoreDependencyKey<?>, CompiledSupplier> compiledRecipeCache = new ConcurrentHashMap<>();

	private HashMap<CoreDependencyKey<?>, SupplierRecipe> recipeCache = new HashMap<>();

	private HashMap<JITBindingKey, JITBinding> jitBindings = new HashMap<>();

	private HashMap<TypeToken<?>, List<StaticBinding>> staticBindingMap = new HashMap<>();
	private ArrayList<StaticBinding> nonTypeSpecificStaticBindings = new ArrayList<>();

	/**
	 * Create and initialize this injector
	 */
	public CoreInjector(CoreInjectorConfiguration config) {
		this.config = config;

		// check uniqueness of static bindings
		{
			HashMap<Matcher<?>, StaticBinding> map = new HashMap<>();
			for (StaticBinding b : config.staticBindings) {
				StaticBinding existing = map.put(b.getMatcher(), b);
				if (existing != null) {
					throw new SaltaException("Duplicate static binding found\n"
							+ b + "\n" + existing);
				}
			}
		}

		// initialize static binding map
		for (StaticBinding binding : config.staticBindings) {
			Set<TypeToken<?>> possibleTypes = binding.getPossibleTypes();
			if (possibleTypes == null || possibleTypes.isEmpty())
				nonTypeSpecificStaticBindings.add(binding);
			else {
				for (TypeToken<?> t : possibleTypes) {
					List<StaticBinding> list = staticBindingMap.get(t);
					if (list == null) {
						list = new ArrayList<>();
						staticBindingMap.put(t, list);
					}
					list.add(binding);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(CoreDependencyKey<T> key) {
		try {
			return (T) getCompiledRecipe(key).get();
		} catch (SaltaException e) {
			throw e;
		} catch (Throwable e) {
			throw new SaltaException("Error while creating instance for " + key
					+ "\n" + e.getMessage(), e);
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
		CompiledSupplier compiledRecipe = compiledRecipeCache.get(key);
		if (compiledRecipe != null)
			return compiledRecipe;

		synchronized (recipeLock) {
			return compiledRecipeCache.computeIfAbsent(key,
					x -> compileSupplier(getRecipe(key)));
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
		// acquire the recipe lock
		synchronized (recipeLock) {
			SupplierRecipe result = recipeCache.get(key);
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
	private SupplierRecipe createRecipe(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		try {
			// check rules
			for (CreationRule rule : config.creationRules) {
				SupplierRecipe recipe = rule.apply(key, ctx);
				if (recipe != null)
					return recipe;
			}

			// check static bindings
			{
				StaticBinding binding = null;
				List<StaticBinding> typeSpecificBindings = staticBindingMap
						.get(key.getType());
				if (typeSpecificBindings == null)
					typeSpecificBindings = Collections.emptyList();

				for (StaticBinding b : Iterables.concat(
						nonTypeSpecificStaticBindings, typeSpecificBindings)) {
					if (b.getMatcher().matches(key)) {
						if (binding != null)
							throw new SaltaException(
									"multiple bindings match dependency " + key
											+ "\n * " + binding + "\n * " + b);
						binding = b;
					}
				}

				if (binding != null) {
					return ctx.getOrCreateRecipe(binding);
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
					return ctx.getOrCreateRecipe(jitBinding);
				}
			}
		} catch (SaltaException e) {
			throw e;
		} catch (Exception e) {
			throw new SaltaException("Error creating recipe for " + key, e);
		}
		throw new SaltaException("Dependency cannot be resolved:\n" + key);
	}

	public RecipeCompiler getCompiler() {
		return compiler;
	}

}

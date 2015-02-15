package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;

public class CoreInjector {

	/**
	 * Lock object for recipe creation and compilation. May not be acquired
	 * while holding the {@link #instantiationLock}
	 */
	public final Object recipeLock = new Object();

	/**
	 * Lock object used during instantiation.
	 */
	public final Object instantiationLock = new Object();

	private CoreInjectorConfiguration config;

	private CreationRecipeCompiler compiler = new CreationRecipeCompiler();

	private ConcurrentHashMap<CoreDependencyKey<?>, CompiledCreationRecipe> compiledRecipeCache = new ConcurrentHashMap<>();

	private HashMap<CoreDependencyKey<?>, CreationRecipe> recipeCache = new HashMap<>();

	private HashMap<JITBindingKey, JITBinding> jitBindings = new HashMap<>();

	private HashMap<TypeToken<?>, List<StaticBinding>> staticBindingMap = new HashMap<>();
	private ArrayList<StaticBinding> nonTypeSpecificStaticBindings = new ArrayList<>();

	/**
	 * Create and initialize this injector
	 */
	public CoreInjector(CoreInjectorConfiguration config) {
		this.config = config;

		// initialize static binding map
		for (StaticBinding binding : config.staticBindings) {
			Set<TypeToken<?>> possibleTypes = binding.getPossibleTypes();
			if (possibleTypes == null)
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

	public <T> T getInstance(CoreDependencyKey<T> key) {
		return getInstanceSupplier(key).get();
	}

	@SuppressWarnings("unchecked")
	public <T> Supplier<T> getInstanceSupplier(CoreDependencyKey<T> key) {
		CompiledCreationRecipe compiledRecipe = getCompiledRecipe(key);
		return () -> {
			try {
				return (T) compiledRecipe.get();
			} catch (ProvisionException e) {
				throw e;
			} catch (Throwable e) {
				throw new ProvisionException(
						"Error while creating instance for " + key, e);
			}
		};
	}

	public CompiledCreationRecipe getCompiledRecipe(CoreDependencyKey<?> key) {
		CompiledCreationRecipe compiledRecipe = compiledRecipeCache.get(key);
		if (compiledRecipe != null)
			return compiledRecipe;

		// possibly slow
		CreationRecipe recipe = getRecipe(key);

		// compile the recipe with a lock on the key. Therefore
		// compile must not do anything fancy
		return compiledRecipeCache.computeIfAbsent(key,
				x -> compileRecipe(recipe));
	}

	public CompiledCreationRecipe compileRecipe(CreationRecipe recipe) {
		synchronized (recipeLock) {
			return compiler.compile(recipe);
		}
	}

	public CompiledParameterizedCreationRecipe compileParameterizedRecipe(
			CreationRecipe recipe) {
		synchronized (recipeLock) {
			return compiler.compileParameterRecipe(recipe);
		}
	}

	public CreationRecipe getRecipe(CoreDependencyKey<?> key) {
		RecipeCreationContextImpl ctx = new RecipeCreationContextImpl(
				CoreInjector.this);
		CreationRecipe result = getRecipe(key, ctx);
		ctx.processQueuedActions();
		return result;
	}

	public CreationRecipe getRecipe(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		// acquire the recipe lock
		synchronized (recipeLock) {
			CreationRecipe result = recipeCache.get(key);
			if (result == null) {
				try {
					result = createRecipe(key, ctx);
				} catch (Exception e) {
					throw new ProvisionException(
							"Error while creating recipe for " + key, e);
				}
				recipeCache.put(key, result);
			}
			return result;
		}
	}

	/**
	 * Create a recipe. Expects the {@link #recipeLock} to be acquired
	 */
	private CreationRecipe createRecipe(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		try {
			// check rules
			for (DependencyFactoryRule rule : config.creationRules) {
				CreationRecipe recipe = rule.apply(key, ctx);
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
					if (b.matches(key)) {
						if (binding != null)
							throw new ProvisionException(
									"multiple bindings match dependency " + key
											+ ": " + binding + ", " + b);
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
		} catch (ProvisionException e) {
			throw e;
		} catch (Exception e) {
			throw new ProvisionException("Error creating recipe for " + key, e);
		}
		throw new ProvisionException("Dependency cannot be resolved:\n" + key);
	}

}

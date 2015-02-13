package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class CoreInjector {

	private CoreInjectorConfiguration config;

	private CreationRecipeCompiler compiler = new CreationRecipeCompiler();

	private final JITBinding nullJitBinding = new JITBinding() {

		@Override
		public CreationRecipe createRecipe(RecipeCreationContext ctx) {
			throw new UnsupportedOperationException(
					"Called createRecipe() of null binding");
		}

	};

	// private Cache<CoreDependencyKey<?>, Supplier<?>> compiledRecipeCache =
	// CacheBuilder
	// .newBuilder().build();
	private Map<CoreDependencyKey<?>, Supplier<?>> compiledRecipeCache = new ConcurrentHashMap<>();

	private Cache<CoreDependencyKey<?>, CreationRecipe> recipeCache = CacheBuilder
			.newBuilder().build();

	private Cache<JITBindingKey, JITBinding> jitBindings = CacheBuilder
			.newBuilder().build();

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

	@SuppressWarnings("unchecked")
	public <T> T getInstance(CoreDependencyKey<T> key) {
		return (T) getCompiledRecipe(key).get();
	}

	@SuppressWarnings("unchecked")
	public <T> Supplier<T> getInstanceSupplier(CoreDependencyKey<T> key) {
		Supplier<?> compiledRecipe = getCompiledRecipe(key);
		return (Supplier<T>) compiledRecipe;
	}

	public Supplier<?> getCompiledRecipe(CoreDependencyKey<?> key) {
		Supplier<?> supplier = compiledRecipeCache.get(key);
		if (supplier != null)
			return supplier;

		// todo: locking
		supplier = compiler.compile(getRecipe(key));
		compiledRecipeCache.put(key, supplier);
		return supplier;
	}

	public CreationRecipe getRecipe(CoreDependencyKey<?> key) {
		return getRecipe(key, new RecipeCreationContextImpl(CoreInjector.this));
	}

	public CreationRecipe getRecipe(CoreDependencyKey<?> key,
			RecipeCreationContext ctx) {
		try {
			return recipeCache.get(key, new Callable<CreationRecipe>() {

				@Override
				public CreationRecipe call() throws Exception {
					return getRecipeInner(key, ctx);
				}
			});
		} catch (UncheckedExecutionException | ExecutionException e) {
			if (e.getCause() instanceof ProvisionException)
				throw (ProvisionException) e.getCause();
			throw new ProvisionException("Error looking up dependency " + key,
					e.getCause());
		}
	}

	private CreationRecipe getRecipeInner(CoreDependencyKey<?> key,
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
					Binding tmp = binding;
					return ctx.withBinding(tmp, () -> tmp.createRecipe(ctx));
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
				try {
					jitBinding = jitBindings.get(jitKey,
							new Callable<JITBinding>() {

								@Override
								public JITBinding call() throws Exception {
									for (JITBindingRule rule : config.jitBindingRules) {
										JITBinding binding = rule.apply(jitKey);
										if (binding != null) {
											return binding;
										}
									}
									return nullJitBinding;
								}
							});
				} catch (ExecutionException e) {
					throw new ProvisionException(
							"Error while evaluating JIT binding rules",
							e.getCause());
				}

				// use binding if available
				if (jitBinding != nullJitBinding) {
					return ctx.withBinding(jitBinding,
							() -> jitBinding.createRecipe(ctx));
				}
			}
		} catch (ProvisionException e) {
			throw e;
		} catch (Exception e) {
			throw new ProvisionException("Error looking up dependency " + key,
					e.getCause());
		}
		throw new ProvisionException("Dependency cannot be resolved:\n" + key);
	}

}

package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class CoreInjector {

	CoreInjectorConfiguration config;

	private final JITBinding nullJitBinding = new JITBinding() {

		@Override
		public CreationRecipe createRecipe() {
			throw new UnsupportedOperationException(
					"Called createRecipe() of null binding");
		}
	};

	private Cache<CoreDependencyKey<?>, Function<ContextualInjector, ?>> keyBasedCache = CacheBuilder
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

	public <T> T getInstance(CoreDependencyKey<T> key) {
		return getInstance(key, createContextualInjector());
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(CoreDependencyKey<T> key,
			ContextualInjector injector) {
		Function<ContextualInjector, ?> factory;
		try {
			factory = keyBasedCache.get(key,
					new Callable<Function<ContextualInjector, ?>>() {

						@Override
						public Function<ContextualInjector, ?> call()
								throws Exception {
							return getInstanceFactory(key);
						}
					});
		} catch (UncheckedExecutionException | ExecutionException e) {
			if (e.getCause() instanceof ProvisionException)
				throw (ProvisionException) e.getCause();
			throw new ProvisionException("Error looking up dependency " + key,
					e.getCause());
		}
		return (T) factory.apply(injector);

	}

	@SuppressWarnings("unchecked")
	public <T> Function<ContextualInjector, T> getInstanceFactory(
			CoreDependencyKey<T> key) {
		try {
			// check rules
			for (DependencyFactoryRule rule : config.creationRules) {
				DependencyFactory<?> factory = rule.apply(key);
				if (factory != null)
					return i -> (T) factory.createInstance(i);
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
					CreationRecipe recipe = binding.createRecipe();
					StaticBinding tmp = binding;
					return injector -> (T) recipe.scope.scope(tmp,
							() -> recipe.createInstance(injector));
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
					CreationRecipe recipe = jitBinding.createRecipe();
					return injector -> (T) recipe.scope.scope(jitBinding,
							() -> recipe.createInstance(injector));
				}
			}
		} catch (ProvisionException e) {
			throw e;
		} catch (Exception e) {
			throw new ProvisionException("Error while creating instance for "
					+ key, e);
		}

		throw new ProvisionException("Dependency cannot be resolved:\n" + key);

	}

	public ContextualInjector createContextualInjector() {
		return new ContextualInjectorImpl(this, new InstantiationContext());
	}

}

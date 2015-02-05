package com.github.ruediste.simpledi.core.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.CreationRecipe;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.InjectorConfiguration;
import com.github.ruediste.simpledi.core.InstanceCreationRule;
import com.github.ruediste.simpledi.core.InstantiationContext;
import com.github.ruediste.simpledi.core.JITBinding;
import com.github.ruediste.simpledi.core.JITBindingKeyRule;
import com.github.ruediste.simpledi.core.JITBindingRule;
import com.github.ruediste.simpledi.core.JitBindingKey;
import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.core.StaticBinding;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;

public class InjectorImpl implements Injector {

	InjectorConfiguration config;

	JITBinding nullJitBinding = new JITBinding() {

		@Override
		public CreationRecipe createRecipe() {
			throw new UnsupportedOperationException(
					"Called createRecipe() of null binding");
		}
	};

	Cache<JitBindingKey, JITBinding> jitBindings = CacheBuilder.newBuilder()
			.build();

	@Override
	public <T> T createInstance(Class<T> cls) {
		return createInstance(new Dependency<T>(cls));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createInstance(Dependency<T> key) {
		InstantiationContext ctx = new InstantiationContext();
		return (T) createInstance(key, ctx);
	}

	public InjectorImpl(InjectorConfiguration config) {
		this.config = config;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	Object createInstance(Dependency<?> dependency, InstantiationContext ctx) {
		// check rules
		for (InstanceCreationRule rule : config.creationRules) {
			Supplier<?> supplier = rule.apply(dependency);
			if (supplier != null)
				return supplier.get();
		}

		// check static bindings
		{
			StaticBinding binding = null;
			for (StaticBinding b : config.staticBindings) {
				if (b.matches(dependency)) {
					if (binding != null)
						throw new ProvisionException(
								"multiple bindings match dependency "
										+ dependency + ": " + binding + ", "
										+ b);
					binding = b;
				}
			}
			if (binding != null) {
				CreationRecipe recipe = binding.createRecipe();
				return recipe.scope.scope(binding,
						() -> createInstance(recipe, ctx));
			}
		}

		// create JIT binding
		{
			// create key
			JitBindingKey key = new JitBindingKey();
			for (JITBindingKeyRule rule : config.jitBindingKeyRules) {
				rule.apply(dependency, key);
			}

			// check existing bindings and create new one if necessary
			JITBinding jitBinding;
			try {
				jitBinding = jitBindings.get(key, new Callable<JITBinding>() {

					@Override
					public JITBinding call() throws Exception {
						for (JITBindingRule rule : config.jitBindingRules) {
							JITBinding binding = rule.apply(key);
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
				return recipe.scope.scope(jitBinding,
						() -> createInstance(recipe, ctx));
			}
		}

		throw new ProvisionException("Dependency cannot be resolved:\n"
				+ dependency);

	}

	private Object createInstance(CreationRecipe recipe,
			InstantiationContext ctx) {
		ContextualInjector injector = new ContextualInjectorImpl(this, ctx);

		return recipe.createInstance(injector);
	}

	@Override
	public void injectMembers(Object instance) {
		config.memberInjectionStrategy.injectMembers(
				TypeToken.of(instance.getClass()), instance);
	}

	@Override
	public <T> void injectMembers(TypeToken<T> type, T instance) {
		config.memberInjectionStrategy.injectMembers(type, instance);
	}
}

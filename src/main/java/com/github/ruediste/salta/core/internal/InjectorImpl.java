package com.github.ruediste.salta.core.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.core.Injector;
import com.github.ruediste.salta.core.InjectorConfiguration;
import com.github.ruediste.salta.core.InstantiationContext;
import com.github.ruediste.salta.core.JITBinding;
import com.github.ruediste.salta.core.JITBindingKeyRule;
import com.github.ruediste.salta.core.JITBindingRule;
import com.github.ruediste.salta.core.JitBindingKey;
import com.github.ruediste.salta.core.NoBindingInstanceCreationRule;
import com.github.ruediste.salta.core.NoBindingInstanceCreator;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.StaticBinding;
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

	/**
	 * Create and initialize this injector
	 */
	public InjectorImpl(InjectorConfiguration config) {
		this.config = config;
		for (Consumer<Injector> i : config.staticInitializers) {
			i.accept(this);
		}
		for (Consumer<Injector> i : config.dynamicInitializers) {
			i.accept(this);
		}
	}

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

	public Object createInstance(Dependency<?> dependency,
			InstantiationContext ctx) {
		try {
			ContextualInjector injector = new ContextualInjectorImpl(this, ctx);
			// check rules
			for (NoBindingInstanceCreationRule rule : config.creationRules) {
				NoBindingInstanceCreator<?> creator = rule.apply(dependency);
				if (creator != null)
					return creator.createInstance(injector);
			}

			// check static bindings
			{
				StaticBinding binding = null;
				for (StaticBinding b : config.staticBindings) {
					if (b.matches(dependency)) {
						if (binding != null)
							throw new ProvisionException(
									"multiple bindings match dependency "
											+ dependency + ": " + binding
											+ ", " + b);
						binding = b;
					}
				}
				if (binding != null) {
					CreationRecipe recipe = binding.createRecipe();
					return recipe.scope.scope(binding,
							() -> recipe.createInstance(injector));
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
					jitBinding = jitBindings.get(key,
							new Callable<JITBinding>() {

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
							() -> recipe.createInstance(injector));
				}
			}
		} catch (Exception e) {
			throw new ProvisionException("Error while creating instance for "
					+ dependency, e);
		}

		throw new ProvisionException("Dependency cannot be resolved:\n"
				+ dependency);

	}

	@Override
	public void injectMembers(Object instance) {
		InstantiationContext ctx = new InstantiationContext();
		config.memberInjectionStrategy.injectMembers(
				TypeToken.of(instance.getClass()), instance,
				new ContextualInjectorImpl(this, ctx));
	}

	@Override
	public <T> void injectMembers(TypeToken<T> type, T instance) {
		InstantiationContext ctx = new InstantiationContext();
		config.memberInjectionStrategy.injectMembers(type, instance,
				new ContextualInjectorImpl(this, ctx));
	}
}

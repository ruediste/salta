package com.github.ruediste.simpledi.internal;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.github.ruediste.simpledi.Binding;
import com.github.ruediste.simpledi.ContextualInjector;
import com.github.ruediste.simpledi.CreationRecipe;
import com.github.ruediste.simpledi.Dependency;
import com.github.ruediste.simpledi.InjectionListener;
import com.github.ruediste.simpledi.Injector;
import com.github.ruediste.simpledi.InjectorConfiguration;
import com.github.ruediste.simpledi.InstanceCreationRule;
import com.github.ruediste.simpledi.InstantiationContext;
import com.github.ruediste.simpledi.Instantiator;
import com.github.ruediste.simpledi.InstantiatorRule;
import com.github.ruediste.simpledi.JITBindingRule;
import com.github.ruediste.simpledi.MembersInjector;
import com.github.ruediste.simpledi.ProvisionException;
import com.github.ruediste.simpledi.Rule;
import com.google.common.reflect.TypeToken;

public class InjectorImpl implements Injector {

	private List<Rule> rules;
	InjectorConfiguration config;
	Map<TypeToken<?>, List<Binding>> bindings = new HashMap<>();

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

		// initialize binding map
		List<String> errors = new ArrayList<>();
		outer: for (Binding binding : config.bindings) {
			List<Binding> list = bindings.get(binding.type);
			if (list == null) {
				list = new ArrayList<>();
				bindings.put(binding.type, list);
			}

			for (Binding b : list) {
				if (b.qualifiers.equals(binding.qualifiers)) {
					errors.add("duplicate binding: " + binding);
					continue outer;
				}
			}

			list.add(binding);
		}

		if (!errors.isEmpty())
			throw new ProvisionException(errors.stream().collect(joining("\n")));
	}

	public <T> Instantiator<T> createInstantiator(TypeToken<T> type) {
		for (InstantiatorRule rule : config.instantiatorRules) {
			Instantiator<T> result = rule.apply(type);
			if (result != null)
				return result;
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	Object createInstance(Dependency<?> dependency, InstantiationContext ctx) {
		// check rules
		for (InstanceCreationRule rule : config.creationRules) {
			Supplier<?> supplier = rule.apply(dependency);
			if (supplier != null)
				return supplier.get();
		}

		// check bindings
		Binding binding = null;
		{
			List<Binding> list = bindings.get(dependency.type);
			if (list != null) {
				for (Binding b : list) {
					if (b.qualifiers.containsAll(dependency.requiredQualifiers)) {
						if (binding != null)
							throw new ProvisionException(
									"multiple bindings match qualifiers "
											+ dependency.requiredQualifiers
											+ ": " + binding + ", " + b);
						binding = b;
					}
				}
			}
		}

		// create JIT binding if necessary
		if (binding == null) {
			for (JITBindingRule rule : config.jitBindingRules) {
				binding = rule.apply(dependency);
				if (binding != null) {
					// check if binding matches dependency
					if (!Objects.equals(dependency.type, binding.type)) {
						throw new ProvisionException(
								"JIT binding has to match the type of the dependency");
					}
					if (!Objects.equals(dependency.requiredQualifiers,
							binding.qualifiers)) {
						throw new ProvisionException(
								"JIT binding has to have exactly the required qualifiers of the dependency");
					}
					break;
				}
			}
		}

		if (binding == null) {
			throw new ProvisionException("Dependency cannot be resolved: "
					+ dependency);
		}

		CreationRecipe recipe = binding.createRecipe();

		if (recipe.scope == null)
			throw new ProvisionException("no scope found for " + dependency);

		return recipe.scope
				.scope((Dependency) dependency,
						() -> {
							ContextualInjector injector = new ContextualInjectorImpl(
									this, ctx);

							Object instance = recipe.instantiator.instantiate(
									(Dependency) dependency, injector);

							// inject members
							for (MembersInjector<?> memberInjector : recipe.membersInjectors) {
								callMemberInjector(memberInjector, instance,
										injector);
							}

							// notify listeners
							for (InjectionListener<?> listener : recipe.injectionListeners) {
								instance = callInjectionLIstener(listener,
										instance, injector);
							}
							return instance;
						});
	}

	@SuppressWarnings("unchecked")
	private <T> void callMemberInjector(MembersInjector<T> memberInjector,
			Object instance, ContextualInjector injector) {
		memberInjector.injectMembers((T) instance, injector);
	}

	@SuppressWarnings("unchecked")
	private <T> T callInjectionLIstener(InjectionListener<T> injectionListener,
			Object instance, ContextualInjector injector) {
		return injectionListener.afterInjection((T) instance, injector);
	}
}

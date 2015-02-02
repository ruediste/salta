package com.github.ruediste.simpledi.internal;

import java.util.List;

import com.github.ruediste.simpledi.ContextualInjector;
import com.github.ruediste.simpledi.CreationRecipe;
import com.github.ruediste.simpledi.InjectionListener;
import com.github.ruediste.simpledi.Injector;
import com.github.ruediste.simpledi.InstantiationContext;
import com.github.ruediste.simpledi.InstanceRequest;
import com.github.ruediste.simpledi.MembersInjector;
import com.github.ruediste.simpledi.ProvisionException;
import com.github.ruediste.simpledi.Rule;

public class InjectorImpl implements Injector {

	private List<Rule> rules;

	@Override
	public <T> T createInstance(Class<T> cls) {
		return createInstance(new InstanceRequest<T>(cls));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createInstance(InstanceRequest<T> key) {
		InstantiationContext ctx = new InstantiationContext();
		return (T) createInstance(key, ctx);
	}

	public InjectorImpl(List<Rule> rules) {
		this.rules = rules;
	}

	Object createInstance(InstanceRequest<?> key, InstantiationContext ctx) {
		CreationRecipe recipe = new CreationRecipe();

		for (Rule rule : rules) {
			rule.apply(recipe, key);
		}

		if (recipe.scope == null)
			throw new ProvisionException("no scope found for " + key);

		return recipe.scope
				.scope(key,
						() -> {
							Object instance = recipe.instantiator.get();

							ContextualInjector injector = new RecursiveInjectorImpl(
									this, ctx);

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

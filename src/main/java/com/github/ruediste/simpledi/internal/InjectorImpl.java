package com.github.ruediste.simpledi.internal;

import java.util.List;
import java.util.function.Supplier;

import com.github.ruediste.simpledi.InjectionListener;
import com.github.ruediste.simpledi.InjectionPoint;
import com.github.ruediste.simpledi.Injector;
import com.github.ruediste.simpledi.InstantiationContext;
import com.github.ruediste.simpledi.InstantiationRecipe;
import com.github.ruediste.simpledi.InstantiationRequest;
import com.github.ruediste.simpledi.Key;
import com.github.ruediste.simpledi.MembersInjector;
import com.github.ruediste.simpledi.ProvisionException;
import com.github.ruediste.simpledi.RecursiveInjector;
import com.github.ruediste.simpledi.Rule;

public class InjectorImpl implements Injector {

	private List<Rule> rules;

	@Override
	public <T> T createInstance(Class<T> cls) {
		return createInstance(new Key<T>(cls));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createInstance(Key<T> key) {
		InstantiationRequest request = new InstantiationRequest(key, null);
		InstantiationContext ctx = new InstantiationContext();
		return (T) createInstance(request, ctx);
	}

	public InjectorImpl(List<Rule> rules) {
		this.rules = rules;
	}

	Object createInstance(InstantiationRequest request, InstantiationContext ctx) {
		InstantiationRecipe recipe = new InstantiationRecipe();
		Supplier<InjectionPoint> injectionPointSupplier = () -> {
			recipe.makeInjectionPointSpecific();
			return request.injectionPoint;
		};

		for (Rule rule : rules) {
			rule.apply(recipe, request.key, injectionPointSupplier);
		}

		if (recipe.scope == null)
			throw new ProvisionException("no scope found for " + request);

		return recipe.scope
				.scope(request,
						() -> {
							Object instance = recipe.instantiator.get();

							RecursiveInjector injector = new RecursiveInjectorImpl(
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
			Object instance, RecursiveInjector injector) {
		memberInjector.injectMembers((T) instance, injector);
	}

	@SuppressWarnings("unchecked")
	private <T> T callInjectionLIstener(InjectionListener<T> injectionListener,
			Object instance, RecursiveInjector injector) {
		return injectionListener.afterInjection((T) instance, injector);
	}
}

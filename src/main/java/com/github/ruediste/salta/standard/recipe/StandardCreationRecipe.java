package com.github.ruediste.salta.standard.recipe;

import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.standard.MembersInjector;

/**
 * Describes how to fulfill a {@link InstantiationRequest}. Created using the
 * {@link Rule}s
 */
public class StandardCreationRecipe extends CreationRecipe {

	public RecipeInstantiator<?> instantiator;
	/**
	 * {@link MembersInjector} get called after the instantiation to inject
	 * fields and methods
	 */
	public final List<RecipeMembersInjector<?>> membersInjectors = new ArrayList<>();
	public final List<RecipeInjectionListener<?>> injectionListeners = new ArrayList<>();

	@Override
	public Object createInstance(ContextualInjector injector) {

		Object instance = instantiator.instantiate(injector);

		// inject members
		for (RecipeMembersInjector<?> memberInjector : membersInjectors) {
			callMemberInjector(memberInjector, instance, injector);
		}

		// notify listeners
		for (RecipeInjectionListener<?> listener : injectionListeners) {
			instance = callInjectionLIstener(listener, instance, injector);
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	private <T> void callMemberInjector(
			RecipeMembersInjector<T> memberInjector, Object instance,
			ContextualInjector injector) {
		memberInjector.injectMembers((T) instance, injector);
	}

	@SuppressWarnings("unchecked")
	private <T> T callInjectionLIstener(
			RecipeInjectionListener<T> injectionListener, Object instance,
			ContextualInjector injector) {
		return injectionListener.afterInjection((T) instance, injector);
	}
}

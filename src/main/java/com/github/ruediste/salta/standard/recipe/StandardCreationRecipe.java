package com.github.ruediste.salta.standard.recipe;

import java.util.ArrayList;
import java.util.List;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.InstantiationContext;
import com.github.ruediste.salta.core.TransitiveCreationRecipe;
import com.github.ruediste.salta.standard.MembersInjector;

/**
 * Describes how to fulfill a {@link InstantiationRequest}. Created using the
 * {@link Rule}s
 */
public class StandardCreationRecipe extends CreationRecipe<Object> {

	public RecipeInstantiator<?> instantiator;
	/**
	 * {@link MembersInjector} get called after the instantiation to inject
	 * fields and methods
	 */
	public final List<RecipeMembersInjector<?>> membersInjectors = new ArrayList<>();
	public final List<RecipeInjectionListener<?>> injectionListeners = new ArrayList<>();

	@Override
	public TransitiveCreationRecipe createTransitive(InstantiationContext ctx) {
		TransitiveRecipeInstantiator transitiveInstantiator = instantiator
				.createTransitive(ctx);

		ArrayList<TransitiveMembersInjector> mem = new ArrayList<>();
		// inject members
		for (RecipeMembersInjector<?> memberInjector : membersInjectors) {
			mem.add(memberInjector.createTransitive(ctx));
		}

		ArrayList<TransitiveRecipeInjectionListener> listen = new ArrayList<>();
		// notify listeners
		for (RecipeInjectionListener<?> listener : injectionListeners) {
			listen.add(listener.createTransitive(ctx));
		}

		return new TransitiveCreationRecipe() {

			@Override
			public Object createInstance(ContextualInjector injector) {
				Object result = transitiveInstantiator.instantiate(injector);
				for (TransitiveMembersInjector membersInjector : mem) {
					membersInjector.injectMembers(result, injector);
				}
				for (TransitiveRecipeInjectionListener listener : listen) {
					result = listener.afterInjection(result, injector);
				}
				return result;
			}

		};
	}

	@Override
	public Object createInstance(ContextualInjector injector) {

		return instantiator.instantiate(injector);
	}

	@Override
	public void injectMembers(Object instance, ContextualInjector injector) {
		// inject members
		for (RecipeMembersInjector<?> memberInjector : membersInjectors) {
			callMemberInjector(memberInjector, instance, injector);
		}

		// notify listeners
		for (RecipeInjectionListener<?> listener : injectionListeners) {
			instance = callInjectionListener(listener, instance, injector);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void callMemberInjector(
			RecipeMembersInjector<T> memberInjector, Object instance,
			ContextualInjector injector) {
		memberInjector.injectMembers((T) instance, injector);
	}

	@SuppressWarnings("unchecked")
	private <T> T callInjectionListener(
			RecipeInjectionListener<T> injectionListener, Object instance,
			ContextualInjector injector) {
		return injectionListener.afterInjection((T) instance, injector);
	}
}

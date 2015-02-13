package com.github.ruediste.salta.standard;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.TransitiveMembersInjector;
import com.github.ruediste.salta.standard.recipe.TransitiveRecipeInjectionListener;
import com.github.ruediste.salta.standard.recipe.TransitiveRecipeInstantiator;
import com.google.common.reflect.TypeToken;

public class DefaultCreationRecipeBuilder {

	public Function<BindingContext, TransitiveRecipeInstantiator> instantiatorSupplier;
	/**
	 * {@link MembersInjector} get called after the instantiation to inject
	 * fields and methods
	 */
	public Function<BindingContext, List<TransitiveMembersInjector>> membersInjectorsSupplier;
	public Function<BindingContext, List<TransitiveRecipeInjectionListener>> injectionListenerSupplier;

	public Supplier<Scope> scopeSupplier;
	private Binding binding;

	public DefaultCreationRecipeBuilder(StandardInjectorConfiguration config,
			TypeToken<?> type, Binding binding) {

		instantiatorSupplier = ctx -> config
				.createRecipeInstantiator(ctx, type);
		membersInjectorsSupplier = ctx -> config.createRecipeMembersInjectors(
				ctx, type);
		injectionListenerSupplier = ctx -> config.createInjectionListeners(ctx,
				type);
		scopeSupplier = () -> config.getScope(type);
	}

	public CreationRecipe build(BindingContext ctx) {

		TransitiveRecipeInstantiator transitiveInstantiator = instantiatorSupplier
				.apply(ctx);

		List<TransitiveMembersInjector> mem = membersInjectorsSupplier
				.apply(ctx);

		List<TransitiveRecipeInjectionListener> listen = injectionListenerSupplier
				.apply(ctx);

		Scope scope = scopeSupplier.get();
		return new CreationRecipe() {

			public Object createInstanceInner() {

				Object result = transitiveInstantiator.instantiate();
				for (TransitiveMembersInjector membersInjector : mem) {
					membersInjector.injectMembers(result);
				}
				for (TransitiveRecipeInjectionListener listener : listen) {
					result = listener.afterInjection(result);
				}
				return result;
			}

			@Override
			public Object createInstance() {
				return scope.scope(binding, () -> createInstanceInner());
			}

		};
	}
}

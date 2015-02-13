package com.github.ruediste.salta.standard;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeInjectionListener;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

public class DefaultCreationRecipeBuilder {

	public Function<BindingContext, RecipeInstantiator> instantiatorSupplier;
	/**
	 * {@link MembersInjector} get called after the instantiation to inject
	 * fields and methods
	 */
	public Function<BindingContext, List<RecipeMembersInjector>> membersInjectorsSupplier;
	public Function<BindingContext, List<RecipeInjectionListener>> injectionListenerSupplier;

	public Supplier<Scope> scopeSupplier;
	private Binding binding;

	public DefaultCreationRecipeBuilder(StandardInjectorConfiguration config,
			TypeToken<?> type, Binding binding) {

		this.binding = binding;
		instantiatorSupplier = ctx -> config
				.createRecipeInstantiator(ctx, type);
		membersInjectorsSupplier = ctx -> config.createRecipeMembersInjectors(
				ctx, type);
		injectionListenerSupplier = ctx -> config.createInjectionListeners(ctx,
				type);
		scopeSupplier = () -> config.getScope(type);
	}

	public CreationRecipe build(BindingContext ctx) {

		RecipeInstantiator transitiveInstantiator = instantiatorSupplier
				.apply(ctx);

		// arrays for performance
		RecipeMembersInjector[] mem = membersInjectorsSupplier.apply(ctx)
				.toArray(new RecipeMembersInjector[] {});

		RecipeInjectionListener[] listen = injectionListenerSupplier
				.apply(ctx).toArray(new RecipeInjectionListener[] {});

		Scope scope = scopeSupplier.get();
		return new CreationRecipe() {

			public Object createInstanceInner() {

				Object result = transitiveInstantiator.instantiate();
				for (RecipeMembersInjector membersInjector : mem) {
					membersInjector.injectMembers(result);
				}
				for (RecipeInjectionListener listener : listen) {
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

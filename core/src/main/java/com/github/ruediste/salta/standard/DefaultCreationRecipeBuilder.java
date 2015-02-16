package com.github.ruediste.salta.standard;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.RecipeInjectionListener;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.google.common.reflect.TypeToken;

public class DefaultCreationRecipeBuilder {

	public Function<RecipeCreationContext, RecipeInstantiator> instantiatorSupplier;
	/**
	 * {@link MembersInjector} get called after the instantiation to inject
	 * fields and methods
	 */
	public Function<RecipeCreationContext, List<RecipeMembersInjector>> membersInjectorsSupplier;
	public Function<RecipeCreationContext, List<RecipeInjectionListener>> injectionListenerSupplier;

	public Supplier<Scope> scopeSupplier;
	private Binding binding;
	private TypeToken<?> type;

	public DefaultCreationRecipeBuilder(StandardInjectorConfiguration config,
			TypeToken<?> type, Binding binding) {

		this.type = type;
		this.binding = binding;
		instantiatorSupplier = ctx -> config
				.createRecipeInstantiator(ctx, type);
		membersInjectorsSupplier = ctx -> config.createRecipeMembersInjectors(
				ctx, type);
		injectionListenerSupplier = ctx -> config.createInjectionListeners(ctx,
				type);
		scopeSupplier = () -> config.getScope(type);
	}

	public CreationRecipe build(RecipeCreationContext ctx) {

		RecipeInstantiator transitiveInstantiator = instantiatorSupplier
				.apply(ctx);

		// arrays for performance
		RecipeMembersInjector[] mem = membersInjectorsSupplier.apply(ctx)
				.toArray(new RecipeMembersInjector[] {});

		RecipeInjectionListener[] listen = injectionListenerSupplier.apply(ctx)
				.toArray(new RecipeInjectionListener[] {});

		CreationRecipe innerRecipe = new CreationRecipe() {

			@Override
			public void compile(GeneratorAdapter mv,
					RecipeCompilationContext compilationContext) {
				transitiveInstantiator.compile(mv, compilationContext);
				for (RecipeMembersInjector membersInjector : mem) {
					membersInjector.compile(mv, compilationContext);
				}
				for (RecipeInjectionListener listener : listen) {
					listener.compile(mv, compilationContext);
				}
			}

		};
		return scopeSupplier.get()
				.createRecipe(ctx, binding, type, innerRecipe);
	}
}

package com.github.ruediste.salta.standard;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
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

	public SupplierRecipe build(RecipeCreationContext ctx) {

		RecipeInstantiator instantiator = instantiatorSupplier.apply(ctx);

		// arrays for performance
		RecipeMembersInjector[] mem = membersInjectorsSupplier.apply(ctx)
				.toArray(new RecipeMembersInjector[] {});

		List<RecipeInjectionListener> listen = injectionListenerSupplier
				.apply(ctx);

		SupplierRecipe seedRecipe = new SupplierRecipe() {

			@Override
			public Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext compilationContext) {
				Class<?> result = instantiator.compile(compilationContext);
				for (RecipeMembersInjector membersInjector : mem) {
					result = membersInjector
							.compile(result, compilationContext);
				}
				return result;
			}
		};

		SupplierRecipe innerRecipe = createInnerRecipe(listen.iterator(),
				seedRecipe);

		return scopeSupplier.get()
				.createRecipe(ctx, binding, type, innerRecipe);
	}

	private SupplierRecipe createInnerRecipe(
			Iterator<RecipeInjectionListener> iterator,
			SupplierRecipe seedRecipe) {
		if (!iterator.hasNext())
			return seedRecipe;

		// consume a listener
		RecipeInjectionListener listener = iterator.next();

		// create a recipe for the rest of the chain
		SupplierRecipe innerRecipe = createInnerRecipe(iterator, seedRecipe);

		// return recipe
		return new SupplierRecipe() {

			@Override
			protected Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext ctx) {
				return listener.compile(ctx, innerRecipe);
			}

		};
	}
}

package com.github.ruediste.salta.standard;

import java.util.ArrayList;
import java.util.Collections;
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
import com.github.ruediste.salta.standard.recipe.RecipeEnhancer;
import com.google.common.reflect.TypeToken;

public class DefaultCreationRecipeBuilder {
	public Function<RecipeCreationContext, SupplierRecipe> constructionRecipeSupplier;
	public Function<RecipeCreationContext, List<RecipeEnhancer>> enhancersSupplier;

	public Supplier<Scope> scopeSupplier;
	private TypeToken<?> type;

	public DefaultCreationRecipeBuilder(StandardInjectorConfiguration config,
			TypeToken<?> boundType) {

		this.type = boundType;
		constructionRecipeSupplier = ctx -> config.createConstructionRecipe(
				ctx, boundType);

		enhancersSupplier = ctx -> config.createEnhancers(ctx, boundType);
		scopeSupplier = () -> config.getScope(boundType);
	}

	/**
	 * Build the recipe.
	 * 
	 * @param binding
	 *            binding to be used for scoping. If null, no scoping will occur
	 */
	public SupplierRecipe build(RecipeCreationContext ctx, Binding binding) {

		// create seed recipe
		SupplierRecipe seedRecipe = constructionRecipeSupplier.apply(ctx);

		// apply enhancers
		List<RecipeEnhancer> enhancers = new ArrayList<>(
				enhancersSupplier.apply(ctx));

		SupplierRecipe innerRecipe = applyEnhancers(seedRecipe, enhancers);

		// apply scope
		if (binding != null) {
			return applyScope(innerRecipe, scopeSupplier.get(), binding, type,
					ctx);
		} else
			return innerRecipe;
	}

	public static SupplierRecipe applyEnhancers(SupplierRecipe seedRecipe,
			List<RecipeEnhancer> enhancers) {
		Collections.reverse(enhancers);
		SupplierRecipe innerRecipe = createInnerRecipe(enhancers.iterator(),
				seedRecipe);
		return innerRecipe;
	}

	public static SupplierRecipe applyScope(SupplierRecipe innerRecipe,
			Scope scope, Binding binding, TypeToken<?> boundType,
			RecipeCreationContext ctx) {
		return scope.createRecipe(ctx, binding, boundType, innerRecipe);
	}

	private static SupplierRecipe createInnerRecipe(
			Iterator<RecipeEnhancer> iterator, SupplierRecipe seedRecipe) {
		if (!iterator.hasNext())
			return seedRecipe;

		// consume an enhancer
		RecipeEnhancer enhancer = iterator.next();

		// create a recipe for the rest of the chain
		SupplierRecipe innerRecipe = createInnerRecipe(iterator, seedRecipe);

		// return recipe
		return new SupplierRecipe() {

			@Override
			protected Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext ctx) {
				return enhancer.compile(ctx, innerRecipe);
			}

		};
	}
}

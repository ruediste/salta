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
	private Binding binding;
	private TypeToken<?> type;
	private StandardInjectorConfiguration config;

	public DefaultCreationRecipeBuilder(StandardInjectorConfiguration config,
			TypeToken<?> type, Binding binding) {

		this.config = config;

		this.type = type;
		this.binding = binding;
		constructionRecipeSupplier = ctx -> config.createConstructionRecipe(
				ctx, type);

		enhancersSupplier = ctx -> config.createEnhancers(ctx, type);
		scopeSupplier = () -> config.getScope(type);
	}

	public SupplierRecipe build(RecipeCreationContext ctx) {

		// create seed recipe
		SupplierRecipe seedRecipe = constructionRecipeSupplier.apply(ctx);

		// apply enhancers
		List<RecipeEnhancer> enhancers = new ArrayList<>(
				enhancersSupplier.apply(ctx));
		Collections.reverse(enhancers);
		SupplierRecipe innerRecipe = createInnerRecipe(enhancers.iterator(),
				seedRecipe);

		config.dynamicInitializers.add(injector -> {
			if (config.stage == Stage.PRODUCTION) {

			}
		});
		// apply scope
		return scopeSupplier.get()
				.createRecipe(ctx, binding, type, innerRecipe);
	}

	private SupplierRecipe createInnerRecipe(Iterator<RecipeEnhancer> iterator,
			SupplierRecipe seedRecipe) {
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

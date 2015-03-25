package com.github.ruediste.salta.standard.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.recipe.RecipeInitializer;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.google.common.reflect.TypeToken;

/**
 * Default {@link ConstructionRule}, using
 * {@link StandardInjectorConfiguration#instantiatorRules},
 * {@link StandardInjectorConfiguration#createRecipeMembersInjectors(RecipeCreationContext, TypeToken)}
 * and {@link StandardInjectorConfiguration#initializerFactories}
 * 
 * @author ruedi
 *
 */
public class DefaultConstructionRule implements ConstructionRule {

	private StandardInjectorConfiguration config;

	public DefaultConstructionRule(StandardInjectorConfiguration config) {
		this.config = config;
	}

	@Override
	public Function<RecipeCreationContext, SupplierRecipe> createConstructionRecipe(
			TypeToken<?> type) {
		// create seed recipe
		Optional<Function<RecipeCreationContext, RecipeInstantiator>> instantiatorFunc = createInstantiationRecipe(type);
		if (!instantiatorFunc.isPresent())
			return null;
		return ctx -> {
			RecipeInstantiator instantiator = instantiatorFunc.get().apply(ctx);
			List<RecipeMembersInjector> memberInjectors = createMembersInjectors(
					ctx, type);
			List<RecipeInitializer> initializers = createInitializers(ctx, type);
			return new SupplierRecipe() {

				@Override
				public Class<?> compileImpl(GeneratorAdapter mv,
						MethodCompilationContext compilationContext) {
					Class<?> result = instantiator.compile(compilationContext);
					for (RecipeMembersInjector membersInjector : memberInjectors) {
						result = membersInjector.compile(result,
								compilationContext);
					}
					// apply initializers
					for (RecipeInitializer initializer : initializers) {
						result = initializer
								.compile(result, compilationContext);
					}
					return result;
				}
			};
		};
	}

	protected List<RecipeInitializer> createInitializers(
			RecipeCreationContext ctx, TypeToken<?> type) {
		return config.defaultRecipe.createInitializers(ctx, type);
	}

	protected List<RecipeMembersInjector> createMembersInjectors(
			RecipeCreationContext ctx, TypeToken<?> type) {
		return config.defaultRecipe.createRecipeMembersInjectors(ctx, type);
	}

	protected Optional<Function<RecipeCreationContext, RecipeInstantiator>> createInstantiationRecipe(
			TypeToken<?> type) {
		return config.defaultRecipe.createRecipeInstantiator(type);
	}
}

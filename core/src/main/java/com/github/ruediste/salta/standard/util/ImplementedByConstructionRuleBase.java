package com.github.ruediste.salta.standard.util;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.config.ConstructionRule;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

public abstract class ImplementedByConstructionRuleBase implements
		ConstructionRule {

	public ImplementedByConstructionRuleBase() {
		super();
	}

	/**
	 * Get the key of the implementation to be used. If null is returned, the
	 * rule does not match
	 */
	protected abstract DependencyKey<?> getImplementorKey(TypeToken<?> type);

	@Override
	public SupplierRecipe createConstructionRecipe(RecipeCreationContext ctx,
			TypeToken<?> type) {
		DependencyKey<?> implementorKey = getImplementorKey(type);
		if (implementorKey != null) {
			SupplierRecipe recipe = ctx.getRecipe(implementorKey);
			return new RecipeInstantiator() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						MethodCompilationContext ctx) {
					return recipe.compile(ctx);
				}

			};
		}

		return null;
	}

}
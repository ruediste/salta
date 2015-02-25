package com.github.ruediste.salta.standard.util;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SupplierRecipe;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.config.InstantiatorRule;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.google.common.reflect.TypeToken;

public abstract class ImplementedByInstantiatorRuleBase implements
		InstantiatorRule {

	public ImplementedByInstantiatorRuleBase() {
		super();
	}

	/**
	 * Get the key of the implementation to be used. If null is returned, the
	 * rule does not match
	 */
	protected abstract DependencyKey<?> getImplementorKey(TypeToken<?> type);

	@Override
	public RecipeInstantiator apply(RecipeCreationContext ctx, TypeToken<?> type) {
		DependencyKey<?> implementorKey = getImplementorKey(type);
		if (implementorKey != null) {
			SupplierRecipe recipe = ctx.getRecipe(implementorKey);
			return new RecipeInstantiator() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						RecipeCompilationContext ctx) {
					return recipe.compile(ctx);
				}

			};
		}

		return null;
	}

}
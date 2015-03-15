package com.github.ruediste.salta.standard.util;

import java.util.Optional;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
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
	public Optional<SupplierRecipe> createConstructionRecipe(
			RecipeCreationContext ctx, TypeToken<?> type) {
		DependencyKey<?> implementorKey = getImplementorKey(type);
		if (implementorKey != null) {
			if (!type.isAssignableFrom(implementorKey.getType())) {
				throw new SaltaException("Implementation " + implementorKey
						+ " specified by @ImplementedBy does not implement "
						+ type);
			}
			SupplierRecipe recipe = ctx.getRecipe(implementorKey);
			return Optional.of(new RecipeInstantiator() {

				@Override
				protected Class<?> compileImpl(GeneratorAdapter mv,
						MethodCompilationContext ctx) {
					return recipe.compile(ctx);
				}

			});
		}

		return Optional.empty();
	}

}
package com.github.ruediste.salta.standard;

import java.util.HashSet;
import java.util.Set;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.StaticBinding;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.matchers.Matcher;
import com.google.common.reflect.TypeToken;

/**
 * Statically defined Binding.
 */
public class StandardStaticBinding extends StaticBinding {
	public Matcher<CoreDependencyKey<?>> dependencyMatcher;
	public final Set<TypeToken<?>> possibleTypes = new HashSet<>();
	public CreationRecipeFactory recipeFactory;

	@Override
	public Set<TypeToken<?>> getPossibleTypes() {
		return possibleTypes;
	}

	@Override
	public SupplierRecipe createRecipe(RecipeCreationContext ctx) {
		return recipeFactory.createRecipe(ctx);
	}

	@Override
	public String toString() {
		return "StandardStaticBinding(" + dependencyMatcher + ")";
	}

	@Override
	public Matcher<CoreDependencyKey<?>> getMatcher() {
		return dependencyMatcher;
	}

}

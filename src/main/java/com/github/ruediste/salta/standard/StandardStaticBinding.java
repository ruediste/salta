package com.github.ruediste.salta.standard;

import java.util.Set;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.StaticBinding;
import com.github.ruediste.salta.matchers.Matcher;
import com.google.common.reflect.TypeToken;

/**
 * Statically defined Binding.
 */
public class StandardStaticBinding extends StaticBinding {
	public Matcher<CoreDependencyKey<?>> dependencyMatcher;
	public Set<TypeToken<?>> possibleTypes;
	public Supplier<CreationRecipe> recipeFactory;

	@Override
	public Set<TypeToken<?>> getPossibleTypes() {
		return possibleTypes;
	}

	@Override
	public boolean matches(CoreDependencyKey<?> dependency) {
		return dependencyMatcher.matches(dependency);
	}

	@Override
	protected CreationRecipe createRecipe() {
		return recipeFactory.get();
	}

	@Override
	public String toString() {
		return "StandardStaticBinding(" + dependencyMatcher + ")";
	}
}

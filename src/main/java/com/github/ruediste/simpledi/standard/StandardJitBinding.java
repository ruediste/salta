package com.github.ruediste.simpledi.standard;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import com.github.ruediste.simpledi.core.CreationRecipe;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.JITBinding;
import com.github.ruediste.simpledi.matchers.Matcher;

/**
 * Statically defined Binding.
 */
public class StandardJitBinding implements JITBinding {
	public final Deque<Consumer<StandardCreationRecipe>> recipeCreationSteps = new ArrayDeque<>();
	public Matcher<Dependency<?>> dependencyMatcher;

	@Override
	public CreationRecipe createRecipe() {
		StandardCreationRecipe recipe = new StandardCreationRecipe();
		for (Consumer<StandardCreationRecipe> step : recipeCreationSteps) {
			step.accept(recipe);
		}
		return recipe;
	}
}

package com.github.ruediste.simpledi.binder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import com.github.ruediste.simpledi.Binding;
import com.github.ruediste.simpledi.CreationRecipe;

/**
 * {@link Binding} implementation used by these binding builders
 */
public class BinderBinding extends Binding {

	Deque<Consumer<CreationRecipe>> recipeCreationSteps = new ArrayDeque<>();

	@Override
	public CreationRecipe createRecipe() {
		CreationRecipe recipe = new CreationRecipe();
		for (Consumer<CreationRecipe> step : recipeCreationSteps) {
			step.accept(recipe);
		}
		return recipe;
	}

}

package com.github.ruediste.salta.standard;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.standard.recipe.RecipeCreationStep;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;

public class StandardBindingBase implements Binding {
	public final Deque<RecipeCreationStep> recipeCreationSteps = new ArrayDeque<>();

	@Override
	public CreationRecipe createRecipe() {
		StandardCreationRecipe recipe = new StandardCreationRecipe();
		for (Consumer<StandardCreationRecipe> step : recipeCreationSteps) {
			step.accept(recipe);
		}
		return recipe;
	}
}

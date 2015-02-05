package com.github.ruediste.simpledi.standard.recipe;

import java.util.function.Consumer;

import com.github.ruediste.simpledi.standard.StandardBindingBase;

/**
 * Step in the creation of a {@link StandardCreationRecipe}, which is invoked
 * from {@link StandardBindingBase#createRecipe()}
 */
public interface RecipeCreationStep extends Consumer<StandardCreationRecipe> {

}

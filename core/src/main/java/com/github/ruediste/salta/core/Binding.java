package com.github.ruediste.salta.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;

/**
 * Base class for bindings
 */
public abstract class Binding extends AttachedPropertyBearerBase {

	private CreationRecipe recipe;
	boolean creatingRecipe;

	public static class RecursiveRecipeCreationDetectedException extends
			ProvisionException {
		private static final long serialVersionUID = 1L;

		RecursiveRecipeCreationDetectedException() {
			super("Recursive recipe creation detected");
		}
	}

	/**
	 * Create the {@link CreationRecipe} for this binding if it does not exist
	 * yet. If the binding is already present, the passed context will be
	 * ignored.
	 */
	public CreationRecipe getOrCreateRecipe(RecipeCreationContext ctx) {
		if (recipe == null) {
			if (creatingRecipe)
				throw new RecursiveRecipeCreationDetectedException();
			creatingRecipe = true;
			recipe = createRecipe(ctx);
			creatingRecipe = false;
		}
		return recipe;
	}

	/**
	 * Create a recipe for this binding. This method will only be called once
	 * per binding. Any expensive operations to create the recipe should be done
	 * in this method
	 */
	protected abstract CreationRecipe createRecipe(RecipeCreationContext ctx);

}

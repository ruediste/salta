package com.github.ruediste.salta.core;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * Base class for {@link StaticBinding}s and {@link JITBinding}s.
 */
public abstract class Binding extends AttachedPropertyBearerBase {

	private SupplierRecipe recipe;
	boolean creatingRecipe;

	public static class RecursiveRecipeCreationDetectedException extends
			SaltaException {
		private static final long serialVersionUID = 1L;

		RecursiveRecipeCreationDetectedException() {
			super("Recursive recipe creation detected");
		}
	}

	/**
	 * Create the {@link CreationRecipe} for this binding if it does not exist
	 * yet. If the binding is already created, the passed context will be
	 * ignored. This method is always called with the
	 * {@link CoreInjector#recipeLock} held, thus no thread synchronization is
	 * needed
	 */
	public SupplierRecipe getOrCreateRecipe(RecipeCreationContext ctx) {
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
	protected abstract SupplierRecipe createRecipe(RecipeCreationContext ctx);

}

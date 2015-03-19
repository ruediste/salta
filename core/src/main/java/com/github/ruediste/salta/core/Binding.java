package com.github.ruediste.salta.core;

import java.util.function.Function;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.github.ruediste.salta.core.compile.SupplierRecipe;

/**
 * Base class for {@link StaticBinding}s and {@link JITBinding}s.
 */
public abstract class Binding extends AttachedPropertyBearerBase {

	private SupplierRecipe recipe;
	private Scope scope;
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
	 * needed.
	 * 
	 * <p>
	 * The returned recipe is ready to be compiled, even before the actions
	 * queued with {@link RecipeCreationContext#queueAction(Runnable)}s are
	 * processed.
	 * </p>
	 */
	public Function<RecipeCreationContext, SupplierRecipe> getOrCreateRecipe() {
		return ctx -> ctx.withBinding(this, () -> {
			if (recipe == null) {
				if (creatingRecipe)
					throw new RecursiveRecipeCreationDetectedException();
				creatingRecipe = true;
				try {
					recipe = createRecipe(ctx);
				} finally {
					creatingRecipe = false;
				}
			}
			return recipe;
		});
	}

	/**
	 * Create a recipe for this binding. This method will only be called once
	 * per binding. Any expensive operations needed for compilation should be
	 * done in this method if possible.
	 * <p>
	 * The returned recipe MUST be ready to be compiled, even before the actions
	 * queued with {@link RecipeCreationContext#queueAction(Runnable)}s are
	 * processed.
	 * </p>
	 */
	protected abstract SupplierRecipe createRecipe(RecipeCreationContext ctx);

	public Scope getScope() {
		if (scope == null)
			scope = getScopeImpl();
		return scope;
	}

	protected abstract Scope getScopeImpl();
}

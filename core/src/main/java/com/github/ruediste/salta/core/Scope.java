package com.github.ruediste.salta.core;

import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.google.common.reflect.TypeToken;

/**
 * A Scope defines a visibility for an instance. The scope can either reuse an
 * instance or decide to create a new instance.
 */
public interface Scope {

	/**
	 * Create a recipe. No incoming parameter on the stack. The scoped instance
	 * is expected afterwards. The calling thread always holds the
	 * {@link CoreInjector#recipeLock}
	 * 
	 * @param ctx
	 * @param binding
	 *            binding which is beeing scoped
	 * @param boundType
	 *            type the binding was created for
	 * @param innerRecipe
	 *            recipe resulting in the unscoped instance
	 * @return
	 */
	SupplierRecipe createRecipe(RecipeCreationContext ctx, Binding binding,
			TypeToken<?> requestedType, SupplierRecipe innerRecipe);

}

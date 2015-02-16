package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.google.common.reflect.TypeToken;

/**
 * A Scope defines a visibility for an instance. The scope can either reuse an
 * instance or decide to create a new instance.
 */
public interface Scope {

	CreationRecipe createRecipe(RecipeCreationContext ctx, Binding binding,
			TypeToken<?> type, CreationRecipe innerRecipe);

}

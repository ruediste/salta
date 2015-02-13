package com.github.ruediste.salta.standard.config;

import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.standard.recipe.RecipeInjectionListener;
import com.github.ruediste.salta.standard.recipe.RecipeInjectionListener;
import com.google.common.reflect.TypeToken;

/**
 * Rule to create an {@link RecipeInjectionListener} for a type
 */
public interface InjectionListenerRule {

	/**
	 * Create a {@link RecipeInjectionListener} based on a type. If null is
	 * returned, the result is ignored
	 * 
	 * @param ctx
	 */
	RecipeInjectionListener getListener(BindingContext ctx,
			TypeToken<?> type);

}

package com.github.ruediste.salta.standard;

import java.util.function.Supplier;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.JITBinding;

/**
 * Statically defined Binding.
 */
public class StandardJitBinding extends JITBinding {

	public Supplier<CreationRecipe<?>> recipeFactory;

	@Override
	protected CreationRecipe createRecipe() {
		return recipeFactory.get();
	}

}

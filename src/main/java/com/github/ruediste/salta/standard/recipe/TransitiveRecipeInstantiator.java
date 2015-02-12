package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.ContextualInjector;

public interface TransitiveRecipeInstantiator {
	Object instantiate(ContextualInjector injector);
}

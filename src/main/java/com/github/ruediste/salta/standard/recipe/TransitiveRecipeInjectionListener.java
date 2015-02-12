package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.ContextualInjector;

public interface TransitiveRecipeInjectionListener {
	Object afterInjection(Object injectee, ContextualInjector injector);
}

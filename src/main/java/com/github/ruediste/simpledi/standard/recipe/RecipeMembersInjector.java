package com.github.ruediste.simpledi.standard.recipe;

import com.github.ruediste.simpledi.core.ContextualInjector;

public interface RecipeMembersInjector<T> {

	void injectMembers(T instance, ContextualInjector injector);
}

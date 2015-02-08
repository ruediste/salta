package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.ContextualInjector;

public interface RecipeMembersInjector<T> {

	void injectMembers(T instance, ContextualInjector injector);

}

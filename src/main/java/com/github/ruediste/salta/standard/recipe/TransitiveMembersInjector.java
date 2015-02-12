package com.github.ruediste.salta.standard.recipe;

import com.github.ruediste.salta.core.ContextualInjector;

public interface TransitiveMembersInjector {
	void injectMembers(Object instance, ContextualInjector injector);
}

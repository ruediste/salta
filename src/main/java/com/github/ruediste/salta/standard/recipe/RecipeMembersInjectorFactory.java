package com.github.ruediste.salta.standard.recipe;

import java.util.List;

import com.google.common.reflect.TypeToken;

/**
 * Factory producing a list of {@link RecipeMembersInjector}s given a type.
 * Usually, multiple factories are used on a single type and all produced
 * injectors are used
 */
public interface RecipeMembersInjectorFactory {
	<T> List<RecipeMembersInjector<T>> createInjectors(TypeToken<T> type);
}

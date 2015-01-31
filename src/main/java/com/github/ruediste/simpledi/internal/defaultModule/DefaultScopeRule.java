package com.github.ruediste.simpledi.internal.defaultModule;

import java.util.function.Supplier;

import com.github.ruediste.simpledi.InjectionPoint;
import com.github.ruediste.simpledi.InstantiationRecipe;
import com.github.ruediste.simpledi.InstantiationRequest;
import com.github.ruediste.simpledi.Key;
import com.github.ruediste.simpledi.Rule;
import com.github.ruediste.simpledi.Scope;

public class DefaultScopeRule implements Rule {

	private final class DefaultScope implements Scope {
		@Override
		public <T> T scope(InstantiationRequest request, Supplier<T> unscoped) {
			return unscoped.get();
		}
	}

	@Override
	public void apply(InstantiationRecipe recipe, Key<?> key,
			Supplier<InjectionPoint> injectionPoint) {
		if (recipe.scope == null)
			recipe.scope = new DefaultScope();
	}

}

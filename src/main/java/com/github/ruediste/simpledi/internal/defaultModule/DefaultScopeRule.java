package com.github.ruediste.simpledi.internal.defaultModule;

import java.util.function.Supplier;

import com.github.ruediste.simpledi.Rule;
import com.github.ruediste.simpledi.core.CreationRecipe;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.Scope;

public class DefaultScopeRule implements Rule {

	private final class DefaultScope implements Scope {
		@Override
		public <T> T scope(Dependency<T> key, Supplier<T> unscoped) {
			return unscoped.get();
		}
	}

	@Override
	public void apply(CreationRecipe recipe, Dependency<?> key) {
		if (recipe.scope == null)
			recipe.scope = new DefaultScope();
	}

}

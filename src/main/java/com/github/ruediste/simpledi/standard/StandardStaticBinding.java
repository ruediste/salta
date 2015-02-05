package com.github.ruediste.simpledi.standard;

import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.StaticBinding;
import com.github.ruediste.simpledi.matchers.Matcher;

/**
 * Statically defined Binding.
 */
public class StandardStaticBinding extends StandardBindingBase implements
		StaticBinding {
	public Matcher<Dependency<?>> dependencyMatcher;

	@Override
	public boolean matches(Dependency<?> dependency) {
		return dependencyMatcher.matches(dependency);
	}

}

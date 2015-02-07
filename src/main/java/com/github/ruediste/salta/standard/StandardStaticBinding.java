package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.core.StaticBinding;
import com.github.ruediste.salta.matchers.Matcher;

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

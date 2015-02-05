package com.github.ruediste.simpledi.standard;

import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.JITBinding;
import com.github.ruediste.simpledi.matchers.Matcher;

/**
 * Statically defined Binding.
 */
public class StandardJitBinding extends StandardBindingBase implements
		JITBinding {
	public Matcher<Dependency<?>> dependencyMatcher;

}

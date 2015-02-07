package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.core.JITBinding;
import com.github.ruediste.salta.matchers.Matcher;

/**
 * Statically defined Binding.
 */
public class StandardJitBinding extends StandardBindingBase implements
		JITBinding {
	public Matcher<Dependency<?>> dependencyMatcher;

}

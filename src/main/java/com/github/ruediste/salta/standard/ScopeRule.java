package com.github.ruediste.salta.standard;

import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * Rule to determine the Scope for a type
 */
public interface ScopeRule {

	/**
	 * If null is returned, the next rule is evaluated. If all rules fail, the
	 * {@link StandardInjectorConfiguration#scopeAnnotationMap} is evaluated. If
	 * still no scope is found, the
	 * {@link StandardInjectorConfiguration#defaultScope} is used.
	 */
	Scope getScope(TypeToken<?> type);
}

package com.github.ruediste.salta.standard;

import java.util.Optional;

import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.google.common.reflect.TypeToken;

/**
 * Rule to determine the Scope for a type
 */
public interface ScopeRule {

	/**
	 * If {@link Optional#empty()} is returned, the next rule is evaluated. If
	 * all rules fail, the
	 * {@link StandardInjectorConfiguration#scopeAnnotationMap} is evaluated. If
	 * still no scope is found, the
	 * {@link StandardInjectorConfiguration#defaultScope} is used.
	 */
	Optional<Scope> getScope(TypeToken<?> type);
}

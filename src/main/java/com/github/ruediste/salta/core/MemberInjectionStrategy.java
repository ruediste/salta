package com.github.ruediste.salta.core;

import com.google.common.reflect.TypeToken;

/**
 * Strategy used by the core to inject members
 */
@Deprecated
public interface MemberInjectionStrategy {

	void injectMembers(TypeToken<?> type, Object instance,
			ContextualInjector injectorImpl);
}

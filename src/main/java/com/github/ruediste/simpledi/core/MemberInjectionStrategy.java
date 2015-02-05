package com.github.ruediste.simpledi.core;

import com.google.common.reflect.TypeToken;

/**
 * Strategy used by the core to inject members
 */
public interface MemberInjectionStrategy {

	void injectMembers(TypeToken<?> type, Object instance);
}

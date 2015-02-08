package com.github.ruediste.salta.standard.config;

import java.util.IdentityHashMap;
import java.util.Map;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.standard.Injector;
import com.google.common.reflect.TypeToken;

/**
 * A token which makes sure that the members of the contained value are injected
 * before the value is returned.
 */
public class MemberInjectionToken<T> {
	private Injector injector;
	private T value;
	private boolean injected;
	private TypeToken<T> type;

	private MemberInjectionToken(Injector injector, T value, TypeToken<T> type) {
		this.injector = injector;
		this.value = value;
		this.type = type;
	}

	public T getValue(ContextualInjector ctxInjector) {
		synchronized (this) {
			if (!injected) {
				injector.injectMembers(type, value, ctxInjector);
				injected = true;
			}
		}
		return value;
	}

	private static final Map<Object, MemberInjectionToken<?>> memberInjectionTokens = new IdentityHashMap<>();

	/**
	 * Create a token to access the value which makes sure that the members of
	 * the value are injected when
	 * {@link MemberInjectionToken#getValue(ContextualInjector)} returns. Only a
	 * single token for a single value (compared by identity) is ever created.
	 */
	@SuppressWarnings("unchecked")
	public static <T> MemberInjectionToken<T> getMemberInjectionToken(
			Injector injector, T value) {
		return getMemberInjectionToken(injector, value,
				(TypeToken<T>) TypeToken.of(value.getClass()));
	}

	/**
	 * Create a token to access the value which makes sure that the members of
	 * the value are injected when
	 * {@link MemberInjectionToken#getValue(ContextualInjector)} returns. Only a
	 * single token for a single value (compared by identity) is ever created.
	 */
	public synchronized static <T> MemberInjectionToken<T> getMemberInjectionToken(
			Injector injector, T value, TypeToken<T> type) {
		@SuppressWarnings("unchecked")
		MemberInjectionToken<T> token = (MemberInjectionToken<T>) memberInjectionTokens
				.get(value);

		if (token == null) {
			token = new MemberInjectionToken<T>(injector, value, type);
			memberInjectionTokens.put(value, token);
		}
		return token;
	}
}
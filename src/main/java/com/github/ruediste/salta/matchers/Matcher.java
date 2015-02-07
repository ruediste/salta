package com.github.ruediste.salta.matchers;

import com.github.ruediste.salta.matchers.Matchers.AndMatcher;
import com.github.ruediste.salta.matchers.Matchers.OrMatcher;

/**
 * Returns {@code true} or {@code false} for a given input.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface Matcher<T> {

	/**
	 * Returns {@code true} if this matches {@code t}, {@code false} otherwise.
	 */
	boolean matches(T t);

	/**
	 * Returns a new matcher which returns {@code true} if both this and the
	 * given matcher return {@code true}.
	 */
	default Matcher<T> and(Matcher<? super T> other) {
		return new AndMatcher<T>(this, other);

	}

	/**
	 * Returns a new matcher which returns {@code true} if either this or the
	 * given matcher return {@code true}.
	 */
	default Matcher<T> or(Matcher<? super T> other) {
		return new OrMatcher<T>(this, other);

	}

}

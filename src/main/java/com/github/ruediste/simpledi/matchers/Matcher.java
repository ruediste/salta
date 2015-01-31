package com.github.ruediste.simpledi.matchers;

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
	Matcher<T> and(Matcher<? super T> other);

	/**
	 * Returns a new matcher which returns {@code true} if either this or the
	 * given matcher return {@code true}.
	 */
	Matcher<T> or(Matcher<? super T> other);
}

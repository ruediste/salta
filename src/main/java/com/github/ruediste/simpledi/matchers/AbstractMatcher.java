package com.github.ruediste.simpledi.matchers;

import java.io.Serializable;

/**
 * Implements {@code and()} and {@code or()}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public abstract class AbstractMatcher<T> implements Matcher<T> {

	@Override
	public Matcher<T> and(final Matcher<? super T> other) {
		return new AndMatcher<T>(this, other);
	}

	@Override
	public Matcher<T> or(Matcher<? super T> other) {
		return new OrMatcher<T>(this, other);
	}

	private static class AndMatcher<T> extends AbstractMatcher<T> implements
			Serializable {
		private final Matcher<? super T> a, b;

		public AndMatcher(Matcher<? super T> a, Matcher<? super T> b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean matches(T t) {
			return a.matches(t) && b.matches(t);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof AndMatcher
					&& ((AndMatcher) other).a.equals(a)
					&& ((AndMatcher) other).b.equals(b);
		}

		@Override
		public int hashCode() {
			return 41 * (a.hashCode() ^ b.hashCode());
		}

		@Override
		public String toString() {
			return "and(" + a + ", " + b + ")";
		}

		private static final long serialVersionUID = 0;
	}

	private static class OrMatcher<T> extends AbstractMatcher<T> implements
			Serializable {
		private final Matcher<? super T> a, b;

		public OrMatcher(Matcher<? super T> a, Matcher<? super T> b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean matches(T t) {
			return a.matches(t) || b.matches(t);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof OrMatcher
					&& ((OrMatcher) other).a.equals(a)
					&& ((OrMatcher) other).b.equals(b);
		}

		@Override
		public int hashCode() {
			return 37 * (a.hashCode() ^ b.hashCode());
		}

		@Override
		public String toString() {
			return "or(" + a + ", " + b + ")";
		}

		private static final long serialVersionUID = 0;
	}
}

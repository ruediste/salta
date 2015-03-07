/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.util;

import javax.annotation.PostConstruct;

import com.github.ruediste.salta.standard.config.MemberInjectionToken;
import com.google.common.base.Objects;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Static utility methods for creating and working with instances of
 * {@link Provider}.
 *
 * @author Kevin Bourrillion (kevinb9n@gmail.com)
 * @since 2.0
 */
public final class Providers {

	private Providers() {
	}

	/**
	 * Returns a provider which always provides {@code instance}. This should
	 * not be necessary to use in your application, but is helpful for several
	 * types of unit tests.
	 *
	 * @param instance
	 *            the instance that should always be provided. This is also
	 *            permitted to be null, to enable aggressive testing, although
	 *            in real life a Guice-supplied Provider will never return null.
	 */
	public static <T> Provider<T> of(final T instance) {
		return new ConstantProvider<T>(instance);
	}

	private static final class GuicifiedProvider<T> implements Provider<T> {
		private javax.inject.Provider<T> delegate;
		private MemberInjectionToken<javax.inject.Provider<T>> token;

		public GuicifiedProvider(javax.inject.Provider<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T get() {
			return token.getValue().get();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(delegate);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GuicifiedProvider<?> other = (GuicifiedProvider<?>) obj;
			return Objects.equal(delegate, other.delegate);
		}

		@Override
		public String toString() {
			return "guicified(jsr330Provider)";
		}

		@PostConstruct
		public void initialize(Injector injector) {
			token = MemberInjectionToken.getMemberInjectionToken(
					injector.getSaltaInjector(), delegate);
		}
	}

	private static final class ConstantProvider<T> implements Provider<T> {
		private final T instance;

		private ConstantProvider(T instance) {
			this.instance = instance;
		}

		@Override
		public T get() {
			return instance;
		}

		@Override
		public String toString() {
			return "of(" + instance + ")";
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof ConstantProvider)
					&& Objects.equal(instance,
							((ConstantProvider<?>) obj).instance);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(instance);
		}
	}

	/**
	 * Returns a Guice-friendly {@code com.google.inject.Provider} for the given
	 * JSR-330 {@code javax.inject.Provider}. The converse method is
	 * unnecessary, since Guice providers directly implement the JSR-330
	 * interface.
	 * 
	 * @since 3.0
	 */
	public static <T> Provider<T> guicify(javax.inject.Provider<T> provider) {
		if (provider instanceof Provider) {
			return (Provider<T>) provider;
		}

		return new GuicifiedProvider<T>(provider);
	}

}

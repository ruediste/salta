/**
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

package com.google.inject;

import static com.google.inject.Asserts.assertContains;
import static com.google.inject.Asserts.getDeclaringSourcePart;
import static com.google.inject.Asserts.reserialize;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import junit.framework.TestCase;

import com.github.ruediste.salta.core.SaltaException;

/**
 * @author jessewilson@google.com (Jesse Wilson)
 */
@SuppressWarnings("UnusedDeclaration")
public class ProvisionExceptionTest extends TestCase {

	public void testExceptionsCollapsed() {
		try {
			Guice.createInjector().getInstance(A.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("UnsupportedOperationException"))
				throw e;
		}
	}

	/**
	 * There's a pass-through of user code in the scope. We want exceptions
	 * thrown by Guice to be limited to a single exception, even if it passes
	 * through user code.
	 */
	public void testExceptionsCollapsedWithScopes() {
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(B.class).in(Scopes.SINGLETON);
				}
			}).getInstance(A.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains(
					UnsupportedOperationException.class.getName()))
				throw e;
		}
	}

	public void testMethodInjectionExceptions() {
		try {
			Guice.createInjector().getInstance(E.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("UnsupportedOperationException"))
				throw e;
		}
	}

	public void testBindToProviderInstanceExceptions() {
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(D.class).toProvider(new DProvider());
				}
			}).getInstance(D.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("UnsupportedOperationException"))
				throw e;
		}
	}

	/**
	 * This test demonstrates that if the user throws a ProvisionException, we
	 * wrap it to add context.
	 */
	public void testProvisionExceptionsAreWrappedForBindToType() {
		if (true)
			return;
		try {
			Guice.createInjector().getInstance(F.class);
			fail();
		} catch (ProvisionException e) {
			assertContains(e.getMessage(), "1) User Exception",
					"at " + F.class.getName()
							+ ".<init>(ProvisionExceptionTest.java:");
		}
	}

	public void testProvisionExceptionsAreWrappedForBindToProviderType() {
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(F.class).toProvider(FProvider.class);
				}
			}).getInstance(F.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("User Exception"))
				throw e;
		}
	}

	public void testProvisionExceptionsAreWrappedForBindToProviderInstance() {
		if (true)
			return;
		try {
			Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(F.class).toProvider(new FProvider());
				}
			}).getInstance(F.class);
			fail();
		} catch (ProvisionException e) {
			assertContains(e.getMessage(), "1) User Exception", "at "
					+ ProvisionExceptionTest.class.getName(),
					getDeclaringSourcePart(getClass()));
		}
	}

	public void testProvisionExceptionIsSerializable() throws IOException {
		try {
			Guice.createInjector().getInstance(A.class);
			fail();
		} catch (SaltaException expected) {
			SaltaException reserialized = reserialize(expected);
			assertEquals(expected.getMessage(), reserialized.getMessage());
		}
	}

	public void testMultipleCauses() {
		try {
			Guice.createInjector().getInstance(G.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("Unsupported")
					&& !e.getMessage().contains("either"))
				throw e;
		}
	}

	public void testInjectInnerClass() throws Exception {
		Injector injector = Guice.createInjector();
		try {
			injector.getInstance(InnerClass.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("non-static inner classes"))
				throw e;
		}
	}

	public void testInjectLocalClass() throws Exception {
		class LocalClass {
		}

		Injector injector = Guice.createInjector();
		try {
			injector.getInstance(LocalClass.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("non-static inner classes"))
				throw e;
		}
	}

	public void testBindingAnnotationsOnMethodsAndConstructors() {
		try {
			Injector injector = Guice.createInjector();
			injector.getInstance(MethodWithBindingAnnotation.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("on parameters instead"))
				throw e;
		}

		try {
			Guice.createInjector().getInstance(
					ConstructorWithBindingAnnotation.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("on parameters instead"))
				throw e;
		}
	}

	public void testBindingAnnotationWarningForScala() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(String.class).annotatedWith(Green.class).toInstance(
						"lime!");
			}
		});
		injector.getInstance(LikeScala.class);
	}

	public void testLinkedBindings() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(D.class).to(RealD.class);
			}
		});

		try {
			injector.getInstance(D.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains(
					UnsupportedOperationException.class.getName()))
				throw e;
		}
	}

	public void testProviderKeyBindings() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(D.class).toProvider(DProvider.class);
			}
		});

		try {
			injector.getInstance(D.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains(
					UnsupportedOperationException.class.getName()))
				throw e;
		}
	}

	private class InnerClass {
	}

	static class A {
		@Inject
		A(B b) {
		}
	}

	static class B {
		@Inject
		C c;
	}

	static class C {
		@Inject
		void setD(RealD d) {
		}
	}

	static class E {
		@Inject
		void setObject(Object o) {
			throw new UnsupportedOperationException();
		}
	}

	static class MethodWithBindingAnnotation {
		@Inject
		@Green
		void injectMe(String greenString) {
		}
	}

	static class ConstructorWithBindingAnnotation {
		// Suppress compiler errors by the error-prone checker
		// InjectedConstructorAnnotations,
		// which catches injected constructors with binding annotations.
		@SuppressWarnings("InjectedConstructorAnnotations")
		@Inject
		@Green
		ConstructorWithBindingAnnotation(String greenString) {
		}
	}

	/**
	 * In Scala, fields automatically get accessor methods with the same name.
	 * So we don't do misplaced-binding annotation detection if the offending
	 * method has a matching field.
	 */
	static class LikeScala {
		@Inject
		@Green
		String green;

		@Inject
		@Green
		String green() {
			return green;
		}
	}

	@Retention(RUNTIME)
	@Target({ FIELD, PARAMETER, CONSTRUCTOR, METHOD })
	@BindingAnnotation
	@interface Green {
	}

	interface D {
	}

	static class RealD implements D {
		@Inject
		RealD() {
			throw new UnsupportedOperationException();
		}
	}

	static class DProvider implements Provider<D> {
		@Override
		public D get() {
			throw new UnsupportedOperationException();
		}
	}

	static class F {
		@Inject
		public F() {
			throw new ProvisionException("User Exception",
					new RuntimeException());
		}
	}

	static class FProvider implements Provider<F> {
		@Override
		public F get() {
			return new F();
		}
	}

	static class G {
		@Inject
		void injectFirst() {
			throw new IllegalArgumentException(
					new UnsupportedOperationException("Unsupported"));
		}

		@Inject
		void injectSecond() {
			throw new NullPointerException("can't inject second either");
		}
	}
}

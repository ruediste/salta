package com.github.ruediste.salta.jsr330.test.wikiChecks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.InjectionOptional;
import com.github.ruediste.salta.jsr330.Salta;

public class InjectionsTest {

	private static class B {
	}

	private class C {
	}

	private static class A {
		@Inject
		@InjectionOptional
		C field;
		private C methodValue;
		private C methodArgValue;
		private B methodB;
		private B methodArgB;
		private C constructorArgValue;
		private B constructorArgB;

		@Inject
		@InjectionOptional
		void method(C value, B b) {
			this.methodValue = value;
			this.methodB = b;
		}

		@Inject
		void methodArg(@InjectionOptional C value, B b) {
			this.methodArgValue = value;
			this.methodArgB = b;
		}

		@Inject
		A(@InjectionOptional C value, B b) {
			this.constructorArgValue = value;
			this.constructorArgB = b;
		}
	}

	@Test
	public void testOptionalNotPresent() {
		A a = Salta.createInjector().getInstance(A.class);

		assertNull(a.field);

		assertNull(a.methodB);
		assertNull(a.methodValue);

		assertNotNull(a.methodArgB);
		assertNull(a.methodArgValue);

		assertNotNull(a.constructorArgB);
		assertNull(a.constructorArgValue);
	}

	@Test
	public void testOptionalPresent() {
		A a = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() throws Exception {
				bind(C.class).toInstance(new C());
			}
		}).getInstance(A.class);

		assertNotNull(a.field);

		assertNotNull(a.methodB);
		assertNotNull(a.methodValue);

		assertNotNull(a.methodArgB);
		assertNotNull(a.methodArgValue);

		assertNotNull(a.constructorArgB);
		assertNotNull(a.constructorArgValue);
	}

	private static class Circular1 {
		@Inject
		Circular2 other;
	}

	private static class Circular2 {
		@Inject
		Circular1 other;
	}

	@Test
	public void circularInstances() {
		try {
			Salta.createInjector(new AbstractModule() {

				@Override
				protected void configure() throws Exception {
					bind(Circular1.class).toInstance(new Circular1());
					bind(Circular2.class).toInstance(new Circular2());
				}
			}).getInstance(Circular1.class);
			fail();
		} catch (SaltaException e) {
			assertTrue(e.getMessage().contains("Recursive recipe creation"));
		}
	}
}

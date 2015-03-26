package com.github.ruediste.salta.jsr330.wikiChecks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.junit.Test;

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
}

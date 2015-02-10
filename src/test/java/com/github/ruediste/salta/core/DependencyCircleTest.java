package com.github.ruediste.salta.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.Injector;

public class DependencyCircleTest {

	private Injector injector;

	static class ConstructorTestClassA {
		public ConstructorTestClassB b;

		ConstructorTestClassA() {
		}

		@Inject
		ConstructorTestClassA(ConstructorTestClassB b) {
			this.b = b;

		}
	}

	static class ConstructorTestClassB {
		public ConstructorTestClassA a;

		@Inject
		ConstructorTestClassB(ConstructorTestClassA a) {
			this.a = a;

		}
	}

	static class FieldTestClassA {
		@Inject
		public FieldTestClassB b;

		@Inject
		FieldTestClassA() {
		}
	}

	static class FieldTestClassB {
		@Inject
		public FieldTestClassA a;

		@Inject
		FieldTestClassB() {
		}
	}

	@Before
	public void setup() {
		injector = Salta.createInjector(new JSR330Module());
	}

	@Test
	public void circularConstructorTest() {
		ConstructorTestClassA a = injector
				.getInstance(ConstructorTestClassA.class);
		assertNotNull(a.b);
		assertNotNull(a.b.a);
		assertNotSame(a, a.b.a);
	}

	@Test
	public void circularFieldTest() {
		FieldTestClassA a = injector.getInstance(FieldTestClassA.class);
		assertNotNull(a.b);
		assertNotNull(a.b.a);
		assertSame(a, a.b.a);
	}
}

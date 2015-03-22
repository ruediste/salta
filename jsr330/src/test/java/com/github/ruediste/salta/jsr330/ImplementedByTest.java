package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

public class ImplementedByTest {
	private Injector injector;

	@ImplementedBy(A.class)
	private interface IA {
		C getC();
	}

	private static class A implements IA {
		private C c;

		@Inject
		public void setC(C c) {
			this.c = c;
		}

		@Override
		public C getC() {
			return c;
		};

	}

	@ImplementedBy(C.class)
	private static class B {
	}

	private static class C extends B {
	}

	@Before
	public void before() {
		injector = Salta.createInjector(new JSR330Module());

	}

	@Test
	public void testInterface() {
		assertNotNull(injector.getInstance(IA.class));
		assertSame(A.class, injector.getInstance(IA.class).getClass());
	}

	@Test
	public void testBaseClass() {
		assertNotNull(injector.getInstance(B.class));
		assertSame(C.class, injector.getInstance(B.class).getClass());
	}

	@Test
	public void AgetsInjected() {
		assertNotNull(injector.getInstance(IA.class).getC());
	}
}

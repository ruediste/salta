package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.standard.Injector;

public class ImplementedByTest {
	@ImplementedBy(A.class)
	private interface IA {
	}

	private static class A implements IA {
	}

	@ImplementedBy(C.class)
	private static class B {
	}

	private static class C extends B {
	}

	@Test
	public void test() {
		Injector injector = Salta.createInjector(new JSR330Module());
		assertNotNull(injector.getInstance(IA.class));
		assertNotSame(B.class, injector.getInstance(B.class).getClass());
	}
}

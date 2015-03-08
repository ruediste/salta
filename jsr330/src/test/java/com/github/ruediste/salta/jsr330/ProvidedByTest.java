package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.standard.binder.InstanceProvider;

public class ProvidedByTest {

	@ProvidedBy(AProvider.class)
	private interface IA {
	}

	private static class A implements IA {
		int value;
	}

	private static class AProvider implements InstanceProvider<IA> {

		@Override
		public IA get() {
			A a = new A();
			a.value = 3;
			return a;
		}

	}

	@Test
	public void test() {
		assertEquals(3, ((A) Salta.createInjector(new JSR330Module())
				.getInstance(IA.class)).value);
	}

	@ProvidedBy(TestBProvider.class)
	private static class TestB {
		@Inject
		public void shouldNotBeCalled(AProvider dummy) {
			fail("Instance constructed by provider should not be injected");
		}
	}

	private static class TestBProvider implements InstanceProvider<TestB> {

		@Override
		public TestB get() {
			return new TestB();
		}

	}

	@Test
	public void constructedInstanceShouldNotBeInjected() {
		Salta.createInjector(new JSR330Module()).getInstance(TestB.class);
	}
}

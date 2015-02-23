package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertEquals;

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
}

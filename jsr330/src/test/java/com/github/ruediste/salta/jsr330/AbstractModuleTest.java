package com.github.ruediste.salta.jsr330;

import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;

public class AbstractModuleTest {

	private static class A {

	}

	@Test(expected = SaltaException.class)
	public void constructorBindingCanThrowException() {
		Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() throws Exception {
				bind(A.class).toConstructor(A.class.getConstructor(int.class));
			}
		}).getInstance(A.class);
	}
}

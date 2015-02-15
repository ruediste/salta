package com.github.ruediste.salta.standard.recipe;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.jsr330.JSR330Module;

public class FixedConstructorRecipeInstantiatorTest {

	@Before
	public void setup() {
		Salta.createInjector(new JSR330Module()).injectMembers(this);
	}

	private static class TestException extends Error {

	}

	private class TestClass {
		public TestClass() {
			throw new TestException();
		}
	}

	@Inject
	Provider<TestClass> p;

	@Test
	public void testCatch() {
		p.get();
	}
}

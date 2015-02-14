package com.github.ruediste.salta.standard.recipe;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.Injector;

public class FixedMethodRecipeMembersInjectorTest {
	private Injector injector;

	private static class TestClass {
		@Inject
		public Object m1() {
			return null;
		}

		@Inject
		public Object m2() {
			return null;
		}
	}

	@Before
	public void setup() {
		injector = Salta.createInjector(new JSR330Module());
	}

	@Test
	public void test() {
		injector.getInstance(TestClass.class);
	}
}

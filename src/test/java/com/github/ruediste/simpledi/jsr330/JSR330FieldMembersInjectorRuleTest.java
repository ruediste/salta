package com.github.ruediste.simpledi.jsr330;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.simpledi.SimpleDi;
import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.ProvisionException;

public class JSR330FieldMembersInjectorRuleTest {

	public static class TestClassA {
		@Inject
		TestClassB b;

		TestClassB b1;

		@Named("foo")
		String c;

	}

	public static class TestClassB {

	}

	public static class TestClassC {

		@Inject
		final TestClassB bFinal = null;
	}

	private Injector injector;

	@Before
	public void before() {
		injector = SimpleDi.createInjector(new JSR330Module());
	}

	@Test
	public void testInjectInjected() {
		TestClassA a = injector.createInstance(TestClassA.class);
		assertNotNull(a.b);
	}

	@Test
	public void testNoInjectAnnotation() {
		TestClassA a = injector.createInstance(TestClassA.class);
		assertNull(a.b1);
		assertNull(a.c);
	}

	@Test(expected = ProvisionException.class)
	public void testFinalNotInjected() {
		injector.createInstance(TestClassC.class);
		fail();
	}
}

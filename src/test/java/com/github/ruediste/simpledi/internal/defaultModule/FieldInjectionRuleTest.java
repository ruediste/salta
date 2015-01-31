package com.github.ruediste.simpledi.internal.defaultModule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.simpledi.Injector;
import com.github.ruediste.simpledi.SimpleDi;

public class FieldInjectionRuleTest {

	private Injector injector;

	@Before
	public void before() {
		injector = SimpleDi.createInjector();
	}

	public static class TestClassA {
		@Inject
		TestClassB b;

		TestClassB b1;

		@Named("foo")
		String c;
	}

	public static class TestClassB {

	}

	@Test
	public void test() {
		TestClassA a = injector.createInstance(TestClassA.class);
		assertNotNull(a.b);
	}

	@Test
	public void testNoInjectAnnotation() {
		TestClassA a = injector.createInstance(TestClassA.class);
		assertNull(a.b1);
		assertNull(a.c);
	}
}

package com.github.ruediste.simpledi;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.junit.Before;
import org.junit.Test;

public class ReflectionUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Qualifier
	@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
	private @interface TestQualifier {

	}

	private static class TestClass {
		@Named("foo")
		public int a;

		@Inject
		@TestQualifier
		public int b;

		@SuppressWarnings("unused")
		@Inject
		public int c;
	}

	@Test
	public void getQualifiers() throws Exception {
		assertEquals(1,
				ReflectionUtil.getQualifiers(TestClass.class.getField("a"))
						.size());
		assertEquals(1,
				ReflectionUtil.getQualifiers(TestClass.class.getField("b"))
						.size());
		assertEquals(0,
				ReflectionUtil.getQualifiers(TestClass.class.getField("c"))
						.size());
	}
}

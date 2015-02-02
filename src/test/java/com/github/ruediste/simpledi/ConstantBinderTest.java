package com.github.ruediste.simpledi;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

public class ConstantBinderTest {

	private Injector injector;

	@Before
	public void before() {
		injector = SimpleDi.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bindConstant().named("foo").to("bar");
			}
		});
	}

	public static class TestClass {
		@Inject
		@Named("foo")
		String c;
	}

	@Test
	public void testDirect() {
		assertEquals("bar", injector.createInstance(new Dependency<String>(
				String.class, ReflectionUtil.createNamed("foo"))));
	}

	@Test
	public void testFieldInject() {
		TestClass a = injector.createInstance(TestClass.class);
		assertEquals("bar", a.c);
	}

}

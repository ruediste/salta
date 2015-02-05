package com.github.ruediste.simpledi.standard.binder;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.simpledi.AbstractModule;
import com.github.ruediste.simpledi.SimpleDi;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.jsr330.JSR330Module;
import com.github.ruediste.simpledi.jsr330.Names;

public class ConstantBindingBuilderTest {

	private Injector injector;

	@Before
	public void before() {
		injector = SimpleDi.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("foo")).to("bar");
			}
		}, new JSR330Module());
	}

	public static class TestClass {
		@Inject
		@Named("foo")
		String c;
	}

	@Test
	public void testDirect() {
		Dependency<String> key = new Dependency<String>(String.class);
		Annotations.dependencyAnnotation.set(key, Names.named("foo"));
		assertEquals("bar", injector.createInstance(key));
	}

	@Test
	public void testFieldInject() {
		TestClass a = injector.createInstance(TestClass.class);
		assertEquals("bar", a.c);
	}

}

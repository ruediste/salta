package com.github.ruediste.salta.jsr330.test.binder;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Salta;

public class AnnotatedBindingBuilderTest {

	private static class TestA {
		@Inject
		String testNoAnnotation;

		@Named("foo")
		@Inject
		String testWithAnnotation;
	}

	@Test
	public void testNamed() throws Exception {
		TestA a = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(String.class).toInstance("bar2");
				bind(String.class).named("foo").toInstance("bar");
			}
		}).getInstance(TestA.class);
		assertEquals("bar2", a.testNoAnnotation);
		assertEquals("bar", a.testWithAnnotation);
	}

}

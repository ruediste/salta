package com.github.ruediste.salta.standard.util;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Scope;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.standard.ScopeImpl;

public class SimpleProxyScopeHandlerTest {

	private Injector injector;

	@Inject
	@Named("batch")
	SimpleProxyScopeHandler handler;

	@Target({ TYPE, METHOD })
	@Retention(RUNTIME)
	@Scope
	private @interface BatchScoped {
	}

	@Before
	public void setup() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() throws Exception {
				SimpleProxyScopeHandler handler = new SimpleProxyScopeHandler(
						"batch");
				bindScope(BatchScoped.class, new ScopeImpl(handler));
				bind(SimpleProxyScopeHandler.class).named("batch").toInstance(
						handler);
			}
		});
		injector.injectMembers(this);
	}

	private static class A {

		@Inject
		B b;
	}

	@BatchScoped
	static class B {
		private int value;

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	@Test
	public void testSimple() {
		A a1 = injector.getInstance(A.class);
		A a2 = injector.getInstance(A.class);

		handler.enter();
		a1.b.setValue(3);
		assertNotSame(a1, a2);
		assertEquals(3, a2.b.getValue());
		handler.exit();

		handler.enter();
		assertEquals(0, a2.b.getValue());
		handler.exit();

	}

	@Test(expected = RuntimeException.class)
	public void testFailureOutOfScope() {
		A a = injector.getInstance(A.class);
		a.b.getValue();
	}
}

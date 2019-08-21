package com.github.ruediste.salta.standard.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.MembersInjector;
import com.github.ruediste.salta.jsr330.Provides;
import com.github.ruediste.salta.jsr330.Salta;

public class ProviderMethodBinderTest {

	private static class TestA {
		private int value;

		TestA(int value) {
			this.value = value;
		}
	}

	@Test
	public void test() {
		TestA a = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
			}

			@Provides
			TestA produceTestA() {
				return new TestA(5);
			}
		}).getInstance(TestA.class);

		assertEquals(5, a.value);
	}

	private class TestB {
	}

	@Test
	public void testGenericParameter() {
		TestA a = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
			}

			@Provides
			TestA produceTestA(MembersInjector<TestB> foo) {
				return new TestA(5);
			}
		}).getInstance(TestA.class);

		assertEquals(5, a.value);
	}

	class TestModuleBase extends AbstractModule {

		@Override
		protected void configure() throws Exception {
		}

		@Provides
		TestA produceTestA(MembersInjector<TestB> foo) {
			return new TestA(5);
		}

		@Provides
		TestB produceTestB() {
			return new TestB();
		}
	}

	@Test
	public void testOverrideProvidesMethos() {
		Injector injector = Salta.createInjector(new TestModuleBase() {

			@Override
			@Provides
			TestA produceTestA(MembersInjector<TestB> foo) {
				return new TestA(6);
			}

			@Override
			TestB produceTestB() {
				return null;
			}
		});
		TestA a = injector.getInstance(TestA.class);

		assertEquals(6, a.value);

		try {
			injector.getInstance(TestB.class);
			fail();
		} catch (SaltaException e) {
			if (!e.getMessage().contains("No instance found for"))
				throw e;
		}
	}
}

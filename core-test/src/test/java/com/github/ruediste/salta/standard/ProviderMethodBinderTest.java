package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.JSR330Module;
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

	private static class TestB {
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
}

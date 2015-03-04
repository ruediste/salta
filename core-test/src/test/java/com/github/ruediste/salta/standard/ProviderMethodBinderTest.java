package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.jsr330.Provides;

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
		}, new JSR330Module()).getInstance(TestA.class);

		assertEquals(5, a.value);
	}
}

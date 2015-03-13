package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

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

	private static class TestB1 {

	}

	private static class TestB {
		@Inject
		MembersInjector<TestB1> i;

		@Inject
		public TestB(MembersInjector<TestB1> i) {
			i.injectMembers(new TestB1());
		}

		@Inject
		public void setInjector(MembersInjector<TestB1> i) {
			i.injectMembers(new TestB1());
		}
	}

	@Test
	public void canInjectMembersInjector() {
		TestB b = Salta.createInjector(new JSR330Module()).getInstance(
				TestB.class);
		b.i.injectMembers(new TestB1());
	}
}

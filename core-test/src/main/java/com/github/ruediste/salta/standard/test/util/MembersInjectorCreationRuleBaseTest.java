package com.github.ruediste.salta.standard.test.util;

import javax.inject.Inject;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.MembersInjector;
import com.github.ruediste.salta.jsr330.Salta;

public class MembersInjectorCreationRuleBaseTest {
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

	private static class TestB1 {

	}

	@Test
	public void canInjectMembersInjector() {
		TestB b = Salta.createInjector().getInstance(TestB.class);
		b.i.injectMembers(new TestB1());
	}
}

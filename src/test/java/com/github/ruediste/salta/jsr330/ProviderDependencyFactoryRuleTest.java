package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertNotSame;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.standard.Injector;

public class ProviderDependencyFactoryRuleTest {

	private Injector injector;

	private static class TestClassA {
		private TestClassB b;

		@Inject
		public TestClassA(TestClassB b) {
			this.b = b;
		}
	}

	private static class TestClassB {
		private Provider<TestClassA> aProvider;

		@Inject
		public TestClassB(Provider<TestClassA> aProvider) {
			this.aProvider = aProvider;
		}
	}

	private static class TestClassC {
		@Inject
		public TestClassC(TestClassD d) {
		}
	}

	private static class TestClassD {
		@Inject
		public TestClassD(Provider<TestClassC> cProvider) {
			cProvider.get();
		}
	}

	@Before
	public void setup() {
		injector = Salta.createInjector(new JSR330Module());
	}

	@Test
	public void testInstantiation() {
		TestClassA a = injector.getInstance(TestClassA.class);
		TestClassA otherA = a.b.aProvider.get();
		assertNotSame(otherA, a);
	}

	@Test(expected = ProvisionException.class)
	public void testForbiddenAccess() {
		injector.getInstance(TestClassC.class);

	}
}

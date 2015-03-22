package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.Injector;

public class JSR330ConstructorInstantiatorRuleTest {

	private Injector injector;

	private static class NoParameter {
		// public NoParameter() {
		// }
	}

	static class OneParameter {
		private NoParameter noParameter;

		@Inject
		public OneParameter(NoParameter noParameter) {
			this.noParameter = noParameter;

		}
	}

	static class Ambigous {
		@SuppressWarnings("unused")
		private NoParameter noParameter;

		@Inject
		public Ambigous() {

		}

		@Inject
		public Ambigous(NoParameter noParameter) {
			this.noParameter = noParameter;

		}
	}

	@Before
	public void before() {
		injector = Salta.createInjector(new JSR330Module());
	}

	@Test
	public void noParameter() {
		NoParameter instance = injector.getInstance(NoParameter.class);
		assertNotNull(instance);
	}

	@Test
	public void oneParameter() {
		OneParameter instance = injector.getInstance(OneParameter.class);
		assertNotNull(instance);
		assertNotNull(instance.noParameter);
	}

	@Test(expected = SaltaException.class)
	public void ambigous() {
		injector.getInstance(Ambigous.class);
	}
}

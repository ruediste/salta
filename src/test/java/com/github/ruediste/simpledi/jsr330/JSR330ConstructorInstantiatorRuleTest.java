package com.github.ruediste.simpledi.jsr330;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.simpledi.SimpleDi;
import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.ProvisionException;

public class JSR330ConstructorInstantiatorRuleTest {

	private Injector injector;

	static class NoParameter {
		public NoParameter() {
		}
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
		injector = SimpleDi.createInjector(new JSR330Module());
	}

	@Test
	public void noParameter() {
		NoParameter instance = injector.createInstance(NoParameter.class);
		assertNotNull(instance);
	}

	@Test
	public void oneParameter() {
		OneParameter instance = injector.createInstance(OneParameter.class);
		assertNotNull(instance);
		assertNotNull(instance.noParameter);
	}

	@Test(expected = ProvisionException.class)
	public void ambigous() {
		injector.createInstance(Ambigous.class);
	}
}

package com.github.ruediste.simpledi.standard.util;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.simpledi.SimpleDi;
import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.jsr330.JSR330Module;

public class ConstructorInstantiatorRuleBaseTest {

	private Injector injector;

	public static class NoParameter {
	}

	private static class OneParameter {
		private NoParameter noParameter;

		@Inject
		public OneParameter(NoParameter noParameter) {
			this.noParameter = noParameter;

		}
	}

	private static class Ambigous {
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
		Ambigous instance = injector.createInstance(Ambigous.class);
		assertNotNull(instance);
	}
}

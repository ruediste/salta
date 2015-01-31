package com.github.ruediste.simpledi.internal.defaultModule;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.simpledi.Injector;
import com.github.ruediste.simpledi.SimpleDi;

public class ConstructorRuleTest {

	private Injector injector;

	public static class NoParameter {

	}

	@Before
	public void before() {
		injector = SimpleDi.createInjector();
	}

	@Test
	public void noParameter() {
		injector.createInstance(NoParameter.class);
	}
}

package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.jsr330.JSR330Module;

public class StandardModuleTest {

	public static class A {
		@Inject
		public A(Stage stage) {

		}
	}

	@Test
	public void canInjectStage() {
		Injector injector = Salta.createInjector(new JSR330Module());
		assertNotNull(injector.getInstance(Stage.class));
		assertNotNull(injector.getInstance(A.class));
	}
}

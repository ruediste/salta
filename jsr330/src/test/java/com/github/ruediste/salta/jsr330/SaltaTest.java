package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SaltaTest {

	@Test
	public void emptyInjector() {
		Injector injector = Salta.createInjector();
		assertNotNull(injector);
	}
}

package com.github.ruediste.salta;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.standard.Injector;

public class SaltaTest {

	@Test
	public void emptyInjector() {
		Injector injector = Salta.createInjector();
		assertNotNull(injector);
	}
}

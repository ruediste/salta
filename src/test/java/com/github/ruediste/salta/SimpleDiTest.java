package com.github.ruediste.salta;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.github.ruediste.salta.SimpleDi;
import com.github.ruediste.salta.core.Injector;

public class SimpleDiTest {

	@Test
	public void emptyInjector() {
		Injector injector = SimpleDi.createInjector();
		assertNotNull(injector);
	}
}

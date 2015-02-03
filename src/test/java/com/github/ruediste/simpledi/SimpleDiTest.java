package com.github.ruediste.simpledi;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.github.ruediste.simpledi.SimpleDi;
import com.github.ruediste.simpledi.core.Injector;

public class SimpleDiTest {

	@Test
	public void emptyInjector() {
		Injector injector = SimpleDi.createInjector();
		assertNotNull(injector);
	}
}

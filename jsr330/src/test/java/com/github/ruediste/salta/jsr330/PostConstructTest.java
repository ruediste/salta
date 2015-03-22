package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertTrue;

import javax.annotation.PostConstruct;

import org.junit.Test;

public class PostConstructTest {

	private static class TestClass {
		boolean initialized;

		@PostConstruct
		private void init() {
			initialized = true;
		}
	}

	@Test
	public void testPostConstructCalled() {
		assertTrue(
				"expected initialized",
				Salta.createInjector(new JSR330Module()).getInstance(
						TestClass.class).initialized);
	}
}

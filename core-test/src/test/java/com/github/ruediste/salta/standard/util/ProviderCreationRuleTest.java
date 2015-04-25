package com.github.ruediste.salta.standard.util;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import org.junit.Test;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.Salta;

public class ProviderCreationRuleTest {

	private static class A {
		@PostConstruct
		void initialize(Provider<B> b) {

		}
	}

	private static class B {
		@SuppressWarnings("unused")
		public B(String canNotInject) {

		}
	}

	@Test
	public void testNotPresentProvider() {
		try {
			Salta.createInjector().getInstance(A.class);
		} catch (SaltaException e) {
			if (!e.getMessage().contains("No recipe found"))
				throw e;
		}
	}
}

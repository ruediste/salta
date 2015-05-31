package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertTrue;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.Salta;

public class SingletonAndProviderTest {

	private static class A {

	}

	@Singleton
	private static class B {
		@Inject
		Provider<A> aProvider;

		private boolean isInitialized;

		@PostConstruct
		void init() {
			aProvider.get();
			isInitialized = true;
		}

		public boolean isInitialized() {
			return isInitialized;
		}

	}

	@Test
	public void providerWorksInPostConstructOfSingleton() {
		B b = Salta.createInjector().getInstance(B.class);
		assertTrue(b.isInitialized());
	}
}

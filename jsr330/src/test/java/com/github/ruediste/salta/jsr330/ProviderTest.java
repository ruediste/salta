package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

import com.github.ruediste.salta.standard.Injector;

public class ProviderTest {

	@Test
	public void providerIsInjected() {
		Provider<String> provider = new Provider<String>() {

			@Inject
			int i;

			@Override
			public String get() {
				return "s" + i;
			}
		};

		Injector injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {

				bind(int.class).toInstance(2);
				bind(String.class).toProvider(provider);
			}
		}, new JSR330Module());

		assertEquals("s2", injector.getInstance(String.class));
	}
}

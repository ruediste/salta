package com.github.ruediste.salta.jsr330;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.junit.Test;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.binder.InstanceProvider;

public class ProviderTest {

	@Test
	public void providerIsInjected() {
		InstanceProvider<String> provider = new InstanceProvider<String>() {

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

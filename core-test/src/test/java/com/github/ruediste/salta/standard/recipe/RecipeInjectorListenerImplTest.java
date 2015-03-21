package com.github.ruediste.salta.standard.recipe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.Injector;

public class RecipeInjectorListenerImplTest {

	private static class TestClass {
	}

	int count;

	@Test
	public void test() {
		Injector injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				binder().bindListener(t -> t.getType().equals(TestClass.class),
						(t, i) -> {
							count++;
							return i;
						});
			}
		}, new JSR330Module());
		count = 0;
		injector.getInstance(TestClass.class);
		assertEquals(1, count);
	}
}

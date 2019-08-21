package com.github.ruediste.salta.standard.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.standard.Stage;
import com.google.common.reflect.TypeToken;

public class StandardModuleTest {

	public static class A {
		@Inject
		public A(Stage stage) {

		}
	}

	@Test
	public void canInjectStage() {
		Injector injector = Salta.createInjector();
		assertNotNull(injector.getInstance(Stage.class));
		assertNotNull(injector.getInstance(A.class));
	}

	@Test
	public void testArrayEquality() {
		Class<?> direct = Integer[].class;
		TypeToken<?> tokenGeneric = new TypeToken<Integer[]>() {
		};

		TypeToken<?> tokenOf = TypeToken.of(direct);

		assertEquals(direct, tokenGeneric.getRawType());
		assertEquals(direct, tokenOf.getRawType());
		assertEquals(tokenGeneric, tokenOf);
		assertEquals(tokenGeneric.hashCode(), tokenOf.hashCode());

	}
}

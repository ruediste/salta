package com.github.ruediste.salta.jsr330.test.wikiChecks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.MembersInjector;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.jsr330.util.JreLoggerCreationRule;
import com.github.ruediste.salta.standard.Stage;
import com.google.common.reflect.TypeToken;

public class BuiltinBindingsTest {

	private Injector injector;

	private static class A {
		@Inject
		Logger log;

		@Inject
		MembersInjector<B> bMembersInjector;

		@Inject
		Injector injector;

		@Inject
		Provider<B> bProvider;

		@Inject
		TypeToken<B> typeToken;

		@Inject
		Stage stage;
	}

	private static class B {
		@Inject
		Logger log;

	}

	@Before
	public void before() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() throws Exception {
				bindCreationRule(new JreLoggerCreationRule());
			}
		});

	}

	@Test
	public void loggerGetsInjected() {
		A a = injector.getInstance(A.class);
		assertEquals(A.class.getName(), a.log.getName());
	}

	@Test
	public void injectorGetsInjected() {
		A a = injector.getInstance(A.class);
		assertSame(injector, a.injector);
	}

	@Test
	public void providerGetsInjected() {
		A a = injector.getInstance(A.class);
		assertNotNull(a.bProvider);
		B b = a.bProvider.get();
		assertNotNull(b);
		assertEquals(B.class.getName(), b.log.getName());
	}

	@Test
	public void typeTokenGetsInjected() {
		A a = injector.getInstance(A.class);
		assertNotNull(a.typeToken);
		assertEquals(TypeToken.of(B.class), a.typeToken);
	}

	private static class C {
		@SuppressWarnings("rawtypes")
		@Inject
		TypeToken token;
	}

	@Test
	public void typeTokenRaw() {
		C c = injector.getInstance(C.class);
		assertEquals(TypeToken.of(TypeToken.class).resolveType(TypeToken.class.getTypeParameters()[0]), c.token);
	}

	@Test
	public void stageGetsInjected() {
		A a = injector.getInstance(A.class);
		assertEquals(Stage.DEVELOPMENT, a.stage);
	}

	@Test
	public void membersInjectorGetsInjected() {
		A a = injector.getInstance(A.class);
		assertNotNull(a.bMembersInjector);
		B b = new B();
		assertNull(b.log);
		a.bMembersInjector.injectMembers(b);
		assertNotNull(b.log);
		assertEquals(B.class.getName(), b.log.getName());
	}
}

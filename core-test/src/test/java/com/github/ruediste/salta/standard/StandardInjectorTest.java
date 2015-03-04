package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.google.common.reflect.TypeToken;

public class StandardInjectorTest {
	private Injector injector;

	@Before
	public void setup() {
		injector = Salta.createInjector(new JSR330Module());
	}

	public static class TestClassA {
	}

	public static class TestClassB {
		@Inject
		TestClassA a;
	}

	public static class TestClassC {
		@Inject
		Provider<TestClassA> a;
	}

	public static class TestClassGeneric<T> {
		@Inject
		T other;
	}

	@Test
	public void testInjectMembers() {
		TestClassB b = new TestClassB();
		assertNull(b.a);
		injector.injectMembers(b);
		assertNotNull(b.a);
	}

	@Test
	public void testInjectMembersGeneric() {
		TestClassGeneric<TestClassA> b = new TestClassGeneric<TestClassA>();
		assertNull(b.other);
		injector.injectMembers(new TypeToken<TestClassGeneric<TestClassA>>() {
			private static final long serialVersionUID = 1L;
		}, b);
		TestClassA other = b.other;
		assertNotNull(other);
	}

	@Test(expected = SaltaException.class)
	public void testInjectMembersGenericFailWithoutTypeToken() {
		TestClassGeneric<TestClassA> b = new TestClassGeneric<TestClassA>();
		injector.injectMembers(b);
	}

	@Test
	public void testInjectProvider() {
		TestClassC c = new TestClassC();
		injector.injectMembers(c);
		assertNotNull(c.a.get());
	}
}

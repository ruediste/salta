package com.github.ruediste.salta.standard;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.SimpleDi;
import com.github.ruediste.salta.core.Injector;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.google.common.reflect.TypeToken;

public class StandardModuleTest {

	private Injector injector;

	@Before
	public void setup() {
		injector = SimpleDi.createInjector(new JSR330Module());
	}

	public static class TestClassA {
	}

	public static class TestClassB {
		@Inject
		TestClassA a;
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

	@Test(expected = ProvisionException.class)
	public void testInjectMembersGenericFailWithoutTypeToken() {
		TestClassGeneric<TestClassA> b = new TestClassGeneric<TestClassA>();
		injector.injectMembers(b);
	}
}

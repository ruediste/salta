package com.github.ruediste.salta.standard.recipe;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.Stage;

public class FixedMethodRecipeMembersInjectorTest {
	private Injector injector;

	private static class TestClass {
		@Inject
		public Object m1() {
			return null;
		}

		@Inject
		public Object m2(Stage stage) {
			return null;
		}

		@Inject
		private Object mPrivate(Stage stage) {
			return null;
		}

		@Inject
		Object mPackage(Stage stage) {
			return null;
		}

		@Inject
		protected Object mProtected(Stage stage) {
			return null;
		}

	}

	public static class TestA {
		@Inject
		public Object publicNonVisible(TestB b) {
			return null;
		}

	}

	private static class TestB {
	}

	@Before
	public void setup() {
		injector = Salta.createInjector(new JSR330Module());
	}

	@Test
	public void testInjectionWorks() {
		injector.getInstance(TestClass.class);
	}

	@Test
	public void testPublicNonVisible() {
		injector.getInstance(TestA.class);
	}

	public static class TestPublic {
		@Inject
		public void foo() {
		}

		@Inject
		public Object withResult() {
			return null;
		}
	}

	@Test
	public void testPublic() {
		injector.getInstance(TestPublic.class);

		TestPublic p = new TestPublic();
		injector.injectMembers(p);
	}
}

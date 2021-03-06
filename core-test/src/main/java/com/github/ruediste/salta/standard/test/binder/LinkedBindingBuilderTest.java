package com.github.ruediste.salta.standard.test.binder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.core.Binding.RecursiveRecipeCreationDetectedException;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.jsr330.util.Names;
import com.github.ruediste.salta.standard.DependencyKey;

public class LinkedBindingBuilderTest {

	private Injector injector;

	@Before
	public void setup() {
		injector = Salta.createInjector();
	}

	private interface TestIA {

	}

	private static class TestA implements TestIA {
		@Inject
		TestB b;

		int injectionCount;

		@Inject
		void setterMethod(TestB b) {
			assertNotNull(b);
			injectionCount++;
		}
	}

	private static class TestB {

	}

	@Test
	public void testToInstance() throws Exception {
		TestA instance = new TestA();
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(TestIA.class).toInstance(instance);
			}
		});

		TestIA retrieved = injector.getInstance(TestIA.class);
		assertSame("original instance retrieved", instance, retrieved);
		assertNotNull("field injeted", instance.b);
	}

	@Test
	public void testToPrimitiveInstance() throws Exception {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(int.class).toInstance(4);
			}
		});

		assertEquals(Integer.valueOf(4), injector.getInstance(int.class));
	}

	@Test
	public void testToInstanceUsedTwiceInjectedOnce() throws Exception {
		TestA instance = new TestA();
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(TestIA.class).annotatedWith(Names.named("x")).toInstance(instance);
				bind(TestIA.class).annotatedWith(Names.named("y")).toInstance(instance);
			}
		});

		TestIA retrievedX = injector.getInstance(DependencyKey.of(TestIA.class).withAnnotations(Names.named("x")));
		assertSame("original instance retrieved", instance, retrievedX);
		assertNotNull("field injeted", instance.b);
		TestIA retrievedY = injector.getInstance(DependencyKey.of(TestIA.class).withAnnotations(Names.named("y")));
		assertSame("original instance retrieved", instance, retrievedY);

		assertEquals("Injected once, althought bound twice", 1, instance.injectionCount);
	}

	private static class TestC1 {
		@Inject
		TestC2 c2;
	}

	private static class TestC2 {
		@Inject
		TestC1 c1;
	}

	@Test
	public void testToInstanceCircular() throws Exception {
		TestC1 c1 = new TestC1();
		TestC2 c2 = new TestC2();
		try {
			injector = Salta.createInjector(new AbstractModule() {

				@Override
				protected void configure() {
					bind(TestC1.class).toInstance(c1);
					bind(TestC2.class).toInstance(c2);
				}
			});
			injector.getInstance(TestC1.class);
		} catch (SaltaException e) {
			if (!e.getRecursiveCauses().anyMatch(x -> x instanceof RecursiveRecipeCreationDetectedException)) {
				throw e;
			}
		}
	}

	private static class ProviderIA implements Provider<TestIA> {

		private TestIA instance;

		@Inject
		TestB b;

		public ProviderIA(TestIA instance) {
			this.instance = instance;
		}

		@Override
		public TestIA get() {
			assertNotNull(b);
			return instance;
		}

		int injectionCount;

		@Inject
		void setterMethod() {
			injectionCount++;
		}
	}

	@Test
	public void testToProviderInstance() {
		TestA instance = new TestA();
		ProviderIA provider = new ProviderIA(instance);

		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(TestIA.class).annotatedWith(Names.named("x")).toProvider(provider);
				bind(TestIA.class).annotatedWith(Names.named("y")).toProvider(provider);
			}
		});

		TestIA retrieved = injector.getInstance(DependencyKey.of(TestIA.class).withAnnotations(Names.named("x")));
		assertSame("original instance retrieved", instance, retrieved);
		assertNull("instance field not injected", instance.b);
		assertNotNull("provider field injected", provider.b);

		assertSame(instance, injector.getInstance(DependencyKey.of(TestIA.class).withAnnotations(Names.named("y"))));
		assertEquals(1, provider.injectionCount);
	}

	private static class ProviderIA1 implements Provider<TestIA> {

		private TestIA instance = new TestA();

		@Inject
		TestB b;

		@Override
		public TestIA get() {
			assertNotNull(b);
			return instance;
		}

		int injectionCount;

		@Inject
		void setterMethod() {
			injectionCount++;
		}
	}

	@Test
	public void testToProviderClass() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(TestIA.class).annotatedWith(Names.named("x")).toProvider(ProviderIA1.class);
				bind(TestIA.class).annotatedWith(Names.named("y")).toProvider(ProviderIA1.class);
			}
		});

		TestA retrievedX = (TestA) injector
				.getInstance(DependencyKey.of(TestIA.class).withAnnotations(Names.named("x")));

		assertNull("instance field not injected", retrievedX.b);

		TestA retrievedY = (TestA) injector
				.getInstance(DependencyKey.of(TestIA.class).withAnnotations(Names.named("y")));
		assertNotSame(retrievedX, retrievedY);

		assertNotSame(retrievedX,
				injector.getInstance(DependencyKey.of(TestIA.class).withAnnotations(Names.named("x"))));
	}

	private static class ProviderIACircular implements Provider<TestIA> {

		@Inject
		TestIA a;

		@Override
		public TestIA get() {

			return a;
		}

	}

	@Test
	public void testToProviderClassCircular() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(TestIA.class).toProvider(ProviderIACircular.class);
			}
		});

		try {
			injector.getInstance(TestIA.class);
		} catch (SaltaException e) {
			if (!e.getMessage().contains("Dependency Circle")) {
				throw e;
			}
		}
	}

	private static class TwoConstructors {
		int value;

		public TwoConstructors() {
			value = 1;
		}

		public TwoConstructors(TestB b) {
			value = 2;
		}
	}

	@Test
	public void testToConstructor() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				try {
					bind(TwoConstructors.class).toConstructor(TwoConstructors.class.getConstructor());
				} catch (NoSuchMethodException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
		});
		assertEquals(1, injector.getInstance(TwoConstructors.class).value);
	}

	private static class TestConstructorMembersInjected {
		public TestConstructorMembersInjected() {

		}

		@Inject
		TestB b;
	}

	@Test
	public void testToConstructorMembersInjected() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				try {
					bind(TestConstructorMembersInjected.class)
							.toConstructor(TestConstructorMembersInjected.class.getConstructor());
				} catch (NoSuchMethodException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
		});

		assertNotNull(injector.getInstance(TestConstructorMembersInjected.class).b);
	}
}

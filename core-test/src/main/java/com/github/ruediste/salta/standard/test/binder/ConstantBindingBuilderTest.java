package com.github.ruediste.salta.standard.test.binder;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContextImpl;
import com.github.ruediste.salta.core.compile.RecipeCompiler;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.jsr330.util.Names;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.binder.StandardConstantBindingBuilder;

public class ConstantBindingBuilderTest {

	private Injector injector;

	@Before
	public void before() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("foo")).to("bar");
				bindConstant().annotatedWith(Names.named("foo")).to(3);
			}
		});
	}

	public static class TestClass {
		@Inject
		@Named("foo")
		String c;

		@Inject
		@Named("foo")
		int i;
	}

	@Test
	public void testCompilation() throws Throwable {
		StandardConstantBindingBuilder builder = new StandardConstantBindingBuilder(null, d -> true);
		StandardStaticBinding binding = builder.createBinding(Integer.class, 3);
		RecipeCompiler compiler = new RecipeCompiler();
		assertEquals(3, compiler.compileSupplier(binding.createRecipe(new RecipeCreationContextImpl(null))).get());
	}

	@Test
	public void testDirect() {
		CoreDependencyKey<String> key = DependencyKey.of(String.class).withAnnotations(Names.named("foo"));
		assertEquals("bar", injector.getInstance(key));
	}

	@Test
	public void testFieldInject() {
		TestClass a = injector.getInstance(TestClass.class);
		assertEquals("bar", a.c);
		assertEquals(3, a.i);
	}

}

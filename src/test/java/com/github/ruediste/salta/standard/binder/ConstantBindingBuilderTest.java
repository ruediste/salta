package com.github.ruediste.salta.standard.binder;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

import com.github.ruediste.salta.AbstractModule;
import com.github.ruediste.salta.Salta;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipeCompiler;
import com.github.ruediste.salta.core.RecipeCreationContextImpl;
import com.github.ruediste.salta.jsr330.JSR330Module;
import com.github.ruediste.salta.jsr330.Names;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.StandardStaticBinding;

public class ConstantBindingBuilderTest {

	private Injector injector;

	@Before
	public void before() {
		injector = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("foo")).to("bar");
			}
		}, new JSR330Module());
	}

	public static class TestClass {
		@Inject
		@Named("foo")
		String c;
	}

	@Test
	public void testCompilation() {
		ConstantBindingBuilder builder = new ConstantBindingBuilder(null,
				d -> true);
		StandardStaticBinding binding = builder.createBinding(Integer.class, 3);
		CreationRecipeCompiler compiler = new CreationRecipeCompiler();
		assertEquals(
				3,
				compiler.compile(
						binding.createRecipe(new RecipeCreationContextImpl(null)))
						.get());
	}

	@Test
	public void testDirect() {
		CoreDependencyKey<String> key = DependencyKey.of(String.class)
				.withAnnotations(Names.named("foo"));
		assertEquals("bar", injector.getInstance(key));
	}

	@Test
	public void testFieldInject() {
		TestClass a = injector.getInstance(TestClass.class);
		assertEquals("bar", a.c);
	}

}

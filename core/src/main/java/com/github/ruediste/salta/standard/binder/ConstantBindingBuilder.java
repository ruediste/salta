package com.github.ruediste.salta.standard.binder;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.CreationRecipeFactory;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

public class ConstantBindingBuilder {

	private StandardInjectorConfiguration config;
	private Matcher<CoreDependencyKey<?>> annotationMatcher;

	public ConstantBindingBuilder(StandardInjectorConfiguration config,
			Matcher<CoreDependencyKey<?>> annotationMatcher) {
		this.config = config;
		this.annotationMatcher = annotationMatcher;
	}

	private void bind(Class<?> cls, Object value) {
		StandardStaticBinding binding = createBinding(cls, value);
		config.config.staticBindings.add(binding);
	}

	StandardStaticBinding createBinding(Class<?> cls, Object value) {
		StandardStaticBinding binding = new StandardStaticBinding();
		binding.dependencyMatcher = annotationMatcher.and(d -> d.getType()
				.isAssignableFrom(cls));
		binding.recipeFactory = new CreationRecipeFactory() {

			@Override
			public CreationRecipe createRecipe(RecipeCreationContext ctx) {
				return new CreationRecipe() {

					@Override
					public void compile(GeneratorAdapter mv,
							RecipeCompilationContext compilationContext) {
						compilationContext.addFieldAndLoad(
								Type.getDescriptor(cls), value);
					}

				};
			}
		};
		return binding;
	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(String value) {
		bind(String.class, value);
	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(int value) {
		bind(Integer.class, value);

	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(long value) {
		bind(Long.class, value);
	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(boolean value) {
		bind(Boolean.class, value);
	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(double value) {
		bind(Double.class, value);
	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(float value) {
		bind(Float.class, value);
	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(short value) {
		bind(Short.class, value);
	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(char value) {
		bind(Character.class, value);
	}

	/**
	 * Binds constant to the given value.
	 * 
	 * @since 3.0
	 */
	public void to(byte value) {
		bind(Byte.class, value);
	}

	/**
	 * Binds constant to the given value.
	 */
	public void to(Class<?> value) {
		bind(Class.class, value);
	}

	/**
	 * Binds constant to the given value.
	 */
	public <E extends Enum<E>> void to(E value) {
		bind(Enum.class, value);
	}
}

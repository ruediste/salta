package com.github.ruediste.salta.standard.binder;

import java.util.function.Supplier;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.matchers.Matcher;
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
		StandardStaticBinding binding = new StandardStaticBinding();
		binding.dependencyMatcher = annotationMatcher.and(d -> d.getType()
				.isAssignableFrom(cls));
		binding.recipeFactory = new Supplier<CreationRecipe>() {

			@Override
			public CreationRecipe get() {
				CreationRecipe recipe = new CreationRecipe() {

					@Override
					public Object createInstance(ContextualInjector injector) {
						return value;
					}
				};
				recipe.scope = config.defaultScope;
				return recipe;
			}
		};
		config.config.staticBindings.add(binding);
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

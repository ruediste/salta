package com.github.ruediste.simpledi.standard.binder;

import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.matchers.Matcher;
import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;
import com.github.ruediste.simpledi.standard.StandardStaticBinding;

public class ConstantBindingBuilder {

	private StandardInjectorConfiguration config;
	private Matcher<Dependency<?>> annotationMatcher;

	public ConstantBindingBuilder(StandardInjectorConfiguration config,
			Matcher<Dependency<?>> annotationMatcher) {
		this.config = config;
		this.annotationMatcher = annotationMatcher;
	}

	private void bind(Class<?> cls, Object value) {
		StandardStaticBinding binding = new StandardStaticBinding();
		binding.dependencyMatcher = annotationMatcher.and(d -> d.type
				.isAssignableFrom(cls));
		binding.recipeCreationSteps.add(r -> {
			r.instantiator = injector -> value;
			r.scope = config.defaultScope;
		});
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
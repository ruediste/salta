package com.github.ruediste.simpledi;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.primitives.Primitives;

public class ConstantBinder {

	private List<Rule> rules;
	private List<Annotation> presentQualifiers;

	public ConstantBinder(List<Annotation> presentQualifiers, List<Rule> rules) {
		this.presentQualifiers = presentQualifiers;
		this.rules = rules;
	}

	private void bind(Class<?> cls, Object value) {
		rules.add(new Rule() {

			@Override
			public void apply(InstantiationRecipe recipe, Key<?> key,
					Supplier<InjectionPoint> injectionPoint) {
				if (!ReflectionUtil.areQualifiersMatching(presentQualifiers,
						key.requiredQualifiers)) {
					return;
				}

				Class<?> rawType = key.type.getRawType();
				boolean matches = cls.equals(rawType);
				if (!matches && Primitives.isWrapperType(cls)) {
					matches |= cls.equals(Primitives.unwrap(rawType));
				}

				if (matches) {
					recipe.instantiator = () -> value;
				}
			}
		});
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

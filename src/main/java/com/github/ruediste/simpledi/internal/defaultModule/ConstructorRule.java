package com.github.ruediste.simpledi.internal.defaultModule;

import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import com.github.ruediste.simpledi.InjectionPoint;
import com.github.ruediste.simpledi.InstantiationRecipe;
import com.github.ruediste.simpledi.Key;
import com.github.ruediste.simpledi.ProvisionException;
import com.github.ruediste.simpledi.Rule;

public class ConstructorRule implements Rule {

	@Override
	public void apply(InstantiationRecipe recipe, Key<?> key,
			Supplier<InjectionPoint> injectionPoint) {
		if (recipe.instantiator != null)
			return;
		Class<?> rawType = key.type.getRawType();

		// cannot instantiate abstract types or interfaces
		if (Modifier.isAbstract(rawType.getModifiers())
				|| rawType.isInterface())
			return;

		recipe.instantiator = () -> {
			try {

				return rawType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ProvisionException("Error while calling constructor",
						e);
			}
		};
	}

}

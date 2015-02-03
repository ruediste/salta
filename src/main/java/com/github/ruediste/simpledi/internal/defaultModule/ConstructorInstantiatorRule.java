package com.github.ruediste.simpledi.internal.defaultModule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.ProviderException;
import java.util.ArrayList;

import javax.inject.Inject;

import com.github.ruediste.simpledi.InstantiatorImpl;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.standard.Instantiator;
import com.github.ruediste.simpledi.standard.InstantiatorRule;
import com.google.common.reflect.TypeToken;

public class ConstructorInstantiatorRule implements InstantiatorRule {

	@Override
	public <T> Instantiator<T> apply(TypeToken<T> type) {
		Constructor<?> noArgsConstructor = null;
		Constructor<?> constructor = null;
		for (Constructor<?> c : type.getRawType().getDeclaredConstructors()) {
			if (Modifier.isStatic(c.getModifiers()))
				continue;

			if (c.isAnnotationPresent(Inject.class)) {
				if (constructor != null)
					throw new ProviderException(
							"Multiple constructors annotated with @Inject found");
				constructor = c;
			}
			if (c.getParameterCount() == 0
					&& !Modifier.isPrivate(c.getModifiers())) {
				noArgsConstructor = c;
			}
		}

		if (constructor == null)
			constructor = noArgsConstructor;

		if (constructor == null) {
			throw new ProviderException(
					"No suitable constructor found for type " + type);
		}

		ArrayList<Dependency<?>> args = new ArrayList<>();
		for (Type parameterType : constructor.getGenericParameterTypes()) {
			// TODO
		}

		return new InstantiatorImpl<T>(constructor, args);
	}

}

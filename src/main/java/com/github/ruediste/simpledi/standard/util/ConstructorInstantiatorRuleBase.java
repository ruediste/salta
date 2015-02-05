package com.github.ruediste.simpledi.standard.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.security.ProviderException;
import java.util.ArrayList;

import com.github.ruediste.simpledi.InstantiatorImpl;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.standard.Instantiator;
import com.github.ruediste.simpledi.standard.InstantiatorRule;
import com.github.ruediste.simpledi.standard.StandardInjectionPoint;
import com.google.common.reflect.TypeToken;

public abstract class ConstructorInstantiatorRuleBase implements
		InstantiatorRule {

	private boolean allowNoArgsConstructor;

	protected ConstructorInstantiatorRuleBase(boolean allowNoArgsConstructor) {
		this.allowNoArgsConstructor = allowNoArgsConstructor;

	}

	@Override
	public <T> Instantiator<T> apply(TypeToken<T> type) {
		Constructor<?> noArgsConstructor = null;
		Constructor<?> constructor = null;
		for (Constructor<?> c : type.getRawType().getDeclaredConstructors()) {
			if (Modifier.isStatic(c.getModifiers()))
				continue;

			if (isInjectableConstructor(c)) {
				if (constructor != null)
					throw new ProvisionException(
							"Multiple constructors annotated with @Inject found on type\n"
									+ type);
				constructor = c;
			}
			if (allowNoArgsConstructor && c.getParameterCount() == 0
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

		Parameter[] parameters = constructor.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Dependency<Object> dependency = new Dependency<Object>(
					(TypeToken) type.resolveType(parameter
							.getParameterizedType()),
					new StandardInjectionPoint(constructor, parameter, i));
			args.add(dependency);
		}

		return new InstantiatorImpl<T>(constructor, args);
	}

	protected abstract boolean isInjectableConstructor(Constructor<?> c);

}

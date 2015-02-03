package com.github.ruediste.simpledi.internal.defaultModule;

import java.lang.reflect.Field;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

import com.github.ruediste.simpledi.DefaultInjectionPoint;
import com.github.ruediste.simpledi.InstantiationRequest;
import com.github.ruediste.simpledi.ReflectionUtil;
import com.github.ruediste.simpledi.Rule;
import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.CreationRecipe;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.MembersInjector;
import com.google.common.reflect.TypeToken;

public class FieldInjectionRule implements Rule {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void apply(CreationRecipe recipe, Dependency<?> key) {

		ArrayList<MembersInjector<?>> injectors = new ArrayList<>();

		TypeToken<?> t = key.type;
		while (t != null) {
			for (Field f : t.getRawType().getDeclaredFields()) {
				if (f.isAnnotationPresent(Inject.class)) {
					f.setAccessible(true);

					// prepare request
					InstantiationRequest request = new InstantiationRequest(
							new Dependency(t.resolveType(f.getGenericType()),
									ReflectionUtil.getQualifiers(f)),
							new DefaultInjectionPoint(f, f));

					injectors.add(new MembersInjector<Object>() {

						@Override
						public void injectMembers(Object instance,
								ContextualInjector injector) {
							try {
								Object value = injector.createInstance(request);
								f.set(instance, value);
							} catch (IllegalArgumentException
									| IllegalAccessException e) {
								throw new ProviderException("Unable to inject "
										+ f.getDeclaringClass().getName() + "."
										+ f.getName(), e);
							}
						}
					});
				}
			}

			Class<?> superclass = t.getRawType().getSuperclass();
			if (superclass == null)
				t = null;
			else
				t = t.getSupertype((Class) superclass);
		}

		// reverse to make sure fields of super classes are injected before
		// those of the subclasses
		Collections.reverse(injectors);
		recipe.membersInjectors.addAll(injectors);
	}

}

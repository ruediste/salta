package com.github.ruediste.simpledi.jsr330;

import javax.inject.Provider;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.NoBindingInstanceCreationRule;
import com.github.ruediste.simpledi.core.NoBindingInstanceCreator;
import com.google.common.reflect.TypeToken;

public class ProviderCreationRule implements NoBindingInstanceCreationRule {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> NoBindingInstanceCreator<T> apply(Dependency<T> dependency) {
		if (Provider.class.equals(dependency.type.getRawType())) {
			TypeToken<?> providedType = dependency.type
					.resolveType(Provider.class.getTypeParameters()[0]);
			Dependency<?> dep = new Dependency(providedType,
					dependency.injectionPoint);

			return new NoBindingInstanceCreator() {

				@Override
				public Provider createInstance(
						ContextualInjector contextualInjector) {
					Injector injector = contextualInjector.getInjector();
					return new Provider() {

						@Override
						public Object get() {
							return injector.createInstance(dep);
						}
					};
				}
			};
		}
		return null;
	}
}

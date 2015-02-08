package com.github.ruediste.salta.jsr330;

import javax.inject.Provider;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CoreInjector;
import com.github.ruediste.salta.core.DependencyFactory;
import com.github.ruediste.salta.core.DependencyFactoryRule;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.google.common.reflect.TypeToken;

public class ProviderDependencyFactoryRule implements DependencyFactoryRule {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> DependencyFactory<T> apply(CoreDependencyKey<T> dependency) {
		if (Provider.class.equals(dependency.getType().getRawType())) {
			TypeToken<?> providedType = dependency.getType().resolveType(
					Provider.class.getTypeParameters()[0]);

			CoreDependencyKey<?> dep;
			if (dependency instanceof InjectionPoint) {
				InjectionPoint p = (InjectionPoint) dependency;
				dep = new InjectionPoint(providedType, p.getMember(),
						p.getAnnotatedElement(), p.getParameterIndex());

			} else {
				dep = DependencyKey.of(providedType).addAnnotations(
						dependency.getAnnotatedElement().getAnnotations());
			}

			return new DependencyFactory() {

				@Override
				public Provider createInstance(
						ContextualInjector contextualInjector) {
					CoreInjector injector = contextualInjector.getInjector();
					return new Provider() {

						@Override
						public Object get() {
							return injector.getInstance(dep);
						}
					};
				}
			};
		}
		return null;
	}
}

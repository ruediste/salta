package com.github.ruediste.salta.jsr330;

import javax.inject.Provider;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.DependencyFactory;
import com.github.ruediste.salta.core.DependencyFactoryRule;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.Injector;
import com.google.common.reflect.TypeToken;

public class ProviderDependencyFactoryRule implements DependencyFactoryRule {

	private Injector injector;

	public ProviderDependencyFactoryRule(Injector injector) {
		this.injector = injector;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> DependencyFactory<T> apply(CoreDependencyKey<T> dependency) {
		if (Provider.class.equals(dependency.getRawType())) {
			TypeToken<?> providedType = dependency.getType().resolveType(
					Provider.class.getTypeParameters()[0]);

			CoreDependencyKey<?> dep;
			if (dependency instanceof InjectionPoint) {
				InjectionPoint p = (InjectionPoint) dependency;
				dep = new InjectionPoint(providedType, p.getMember(),
						p.getAnnotatedElement(), p.getParameterIndex());

			} else {
				dep = DependencyKey.of(providedType).withAnnotations(
						dependency.getAnnotatedElement().getAnnotations());
			}

			return new DependencyFactory() {

				@Override
				public Provider createInstance(
						ContextualInjector contextualInjector) {
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

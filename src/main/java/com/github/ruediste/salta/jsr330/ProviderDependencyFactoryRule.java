package com.github.ruediste.salta.jsr330;

import java.util.function.Function;

import javax.inject.Provider;

import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.DependencyFactoryRule;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.Injector;
import com.google.common.reflect.TypeToken;

public class ProviderDependencyFactoryRule implements DependencyFactoryRule {

	private Injector injector;

	ProviderDependencyFactoryRule(Injector injector) {
		this.injector = injector;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Function<BindingContext, CreationRecipe> apply(
			CoreDependencyKey<?> dependency) {

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

			return ctx -> new CreationRecipe() {

				@Override
				public Object createInstance() {
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

package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {

	public AnnotatedBindingBuilder(Injector injector,
			StandardStaticBinding binding,
			DependencyKey<T> eagerInstantiationKey,
			StandardInjectorConfiguration config) {
		super(injector, binding, eagerInstantiationKey, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> annotationType) {
		binding.dependencyMatcher = binding.dependencyMatcher.and(Annotations
				.matcher(annotationType));

		eagerInstantiationDependency.addAnnotation(annotationType);

		return new LinkedBindingBuilder<>(injector, binding,
				eagerInstantiationDependency, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
		binding.dependencyMatcher = binding.dependencyMatcher.and(Annotations
				.matcher(annotation));

		eagerInstantiationDependency.addAnnotation(annotation);

		return new LinkedBindingBuilder<>(injector, binding,
				eagerInstantiationDependency, config);
	}

}

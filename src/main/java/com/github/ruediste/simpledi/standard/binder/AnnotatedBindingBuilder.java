package com.github.ruediste.simpledi.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;
import com.github.ruediste.simpledi.standard.StandardStaticBinding;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {

	public AnnotatedBindingBuilder(StandardStaticBinding binding,
			Dependency<T> eagerInstantiationKey,
			StandardInjectorConfiguration config) {
		super(binding, eagerInstantiationKey, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> annotationType) {
		binding.dependencyMatcher = binding.dependencyMatcher.and(Annotations
				.matcher(annotationType));

		Dependency<T> newDependency = new Dependency<T>(
				eagerInstantiationDependency);
		Annotations.dependencyAnnotationClass
				.set(newDependency, annotationType);

		return new LinkedBindingBuilder<>(binding, newDependency, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
		binding.dependencyMatcher = binding.dependencyMatcher.and(Annotations
				.matcher(annotation));

		Dependency<T> newDependency = new Dependency<T>(
				eagerInstantiationDependency);
		Annotations.dependencyAnnotation.set(newDependency, annotation);

		return new LinkedBindingBuilder<>(binding, newDependency, config);
	}

}

package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {

	public AnnotatedBindingBuilder(BindingBuilderData<T> data) {
		super(data);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> annotationType) {
		data.binding.dependencyMatcher = data.binding.dependencyMatcher
				.and(Annotations.matcher(annotationType));

		data.eagerInstantiationDependency = data.eagerInstantiationDependency
				.withAnnotations(annotationType);

		return new LinkedBindingBuilder<>(data);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
		data.annotationMatcher = Annotations.matcher(annotation);
		data.updateDepenencyMatcher();
		data.binding.dependencyMatcher = data.binding.dependencyMatcher
				.and(Annotations.matcher(annotation));

		data.eagerInstantiationDependency = data.eagerInstantiationDependency
				.withAnnotations(annotation);

		return new LinkedBindingBuilder<>(data);
	}

}

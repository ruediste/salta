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
	@SuppressWarnings("unchecked")
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> availableAnnotationType) {

		data.setAnnotationMatcher(data.config
				.requredQualifierMatcher(availableAnnotationType));

		data.eagerInstantiationDependency = data.eagerInstantiationDependency
				.withAnnotations(availableAnnotationType);

		return new LinkedBindingBuilder<>(data);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(Annotation availableAnnotation) {
		data.setAnnotationMatcher(data.config
				.requredQualifierMatcher(availableAnnotation));
		data.eagerInstantiationDependency = data.eagerInstantiationDependency
				.withAnnotations(availableAnnotation);

		return new LinkedBindingBuilder<>(data);
	}
}

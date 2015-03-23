package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

/**
 * See the EDSL examples at {@link SaltaBinder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {

	/**
	 * See the EDSL examples at {@link SaltaBinder}.
	 */
	LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> availableAnnotationType);

	/**
	 * See the EDSL examples at {@link SaltaBinder}.
	 */
	LinkedBindingBuilder<T> annotatedWith(Annotation availableAnnotation);
}

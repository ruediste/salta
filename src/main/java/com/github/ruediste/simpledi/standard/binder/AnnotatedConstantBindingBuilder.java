package com.github.ruediste.simpledi.standard.binder;

import java.lang.annotation.Annotation;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface AnnotatedConstantBindingBuilder {

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	ConstantBindingBuilder annotatedWith(
			Class<? extends Annotation> annotationType);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	ConstantBindingBuilder annotatedWith(Annotation annotation);
}

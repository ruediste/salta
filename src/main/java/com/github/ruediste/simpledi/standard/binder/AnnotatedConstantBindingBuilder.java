package com.github.ruediste.simpledi.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class AnnotatedConstantBindingBuilder {

	private StandardInjectorConfiguration config;

	public AnnotatedConstantBindingBuilder(StandardInjectorConfiguration config) {
		this.config = config;
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ConstantBindingBuilder annotatedWith(
			Class<? extends Annotation> annotationType) {
		return new ConstantBindingBuilder(config,
				Annotations.matcher(annotationType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ConstantBindingBuilder annotatedWith(Annotation annotation) {
		return new ConstantBindingBuilder(config,
				Annotations.matcher(annotation));
	}
}

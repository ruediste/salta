package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

/**
 * See the EDSL examples at {@link SaltaBinder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class AnnotatedConstantBindingBuilder {

	private StandardInjectorConfiguration config;

	public AnnotatedConstantBindingBuilder(StandardInjectorConfiguration config) {
		this.config = config;
	}

	/**
	 * See the EDSL examples at {@link SaltaBinder}.
	 */
	public ConstantBindingBuilder annotatedWith(
			Class<? extends Annotation> annotationType) {
		return new ConstantBindingBuilder(config,
				config.requredQualifierMatcher(annotationType));
	}

	/**
	 * See the EDSL examples at {@link SaltaBinder}.
	 */
	public ConstantBindingBuilder annotatedWith(Annotation annotation) {
		return new ConstantBindingBuilder(config,
				config.requredQualifierMatcher(annotation));
	}

	@Override
	public String toString() {
		return "ConstantBindingBuilder";
	}
}

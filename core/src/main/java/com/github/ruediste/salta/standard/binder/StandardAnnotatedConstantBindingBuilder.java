package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

/**
 * See the EDSL examples at {@link StandardBinder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class StandardAnnotatedConstantBindingBuilder {

	private StandardInjectorConfiguration config;

	public StandardAnnotatedConstantBindingBuilder(StandardInjectorConfiguration config) {
		this.config = config;
	}

	/**
	 * See the EDSL examples at {@link StandardBinder}.
	 */
	public StandardConstantBindingBuilder annotatedWith(
			Class<? extends Annotation> annotationType) {
		return new StandardConstantBindingBuilder(config,
				config.requredQualifierMatcher(annotationType));
	}

	/**
	 * See the EDSL examples at {@link StandardBinder}.
	 */
	public StandardConstantBindingBuilder annotatedWith(Annotation annotation) {
		return new StandardConstantBindingBuilder(config,
				config.requredQualifierMatcher(annotation));
	}

	@Override
	public String toString() {
		return "ConstantBindingBuilder";
	}
}

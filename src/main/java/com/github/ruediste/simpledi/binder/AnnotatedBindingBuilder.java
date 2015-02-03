package com.github.ruediste.simpledi.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.simpledi.InstanceRequestEnricher;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.InjectionPoint;
import com.github.ruediste.simpledi.core.InjectorConfiguration;
import com.github.ruediste.simpledi.matchers.AbstractMatcher;
import com.github.ruediste.simpledi.matchers.Matcher;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {

	public static AttachedProperty<Dependency<?>, Class<?>> keyAnnotationClass = new AttachedProperty<>(
			"keyAnnotationClass");
	public static AttachedProperty<Dependency<?>, Annotation> keyAnnotation = new AttachedProperty<>(
			"keyAnnotation");

	public AnnotatedBindingBuilder(Matcher<Dependency<?>> keyMatcher,
			Dependency<?> eagerInstantiationKey, InjectorConfiguration config) {
		super(keyMatcher, eagerInstantiationKey, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> annotationType) {
		config.addKeyEnricher(new InstanceRequestEnricher() {

			@Override
			public void enrich(Dependency<?> key, InjectionPoint injectionPoint) {
				if (keyMatcher.matches(key)
						&& injectionPoint.getAnnotated().isAnnotationPresent(
								annotationType)) {
					keyAnnotationClass.set(key, annotationType);
				}
			}
		});

		Dependency<?> newKey = new Dependency<>(eagerInstantiationKey);
		keyAnnotationClass.set(newKey, annotationType);

		return new LinkedBindingBuilder<>(
				keyMatcher.and(new AbstractMatcher<Dependency<?>>() {

					@Override
					public boolean matches(Dependency<?> t) {
						return annotationType.equals(keyAnnotationClass.get(t));
					}
				}), newKey, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
		config.addKeyEnricher(new InstanceRequestEnricher() {

			@Override
			public void enrich(Dependency<?> key, InjectionPoint injectionPoint) {
				Annotation present = injectionPoint.getAnnotated()
						.getAnnotation(annotation.getClass());
				if (keyMatcher.matches(key) && annotation.equals(present)) {
					keyAnnotation.set(key, present);
				}
			}
		});

		Dependency<?> newKey = new Dependency<>(eagerInstantiationKey);
		keyAnnotation.set(newKey, annotation);

		return new LinkedBindingBuilder<>(
				keyMatcher.and(new AbstractMatcher<Dependency<?>>() {

					@Override
					public boolean matches(Dependency<?> t) {
						return annotation.equals(keyAnnotation.get(t));
					}
				}), newKey, config);
	}
}

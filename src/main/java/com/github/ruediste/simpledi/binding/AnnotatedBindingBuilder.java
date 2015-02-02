package com.github.ruediste.simpledi.binding;

import java.lang.annotation.Annotation;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.simpledi.InjectionPoint;
import com.github.ruediste.simpledi.InjectorConfiguration;
import com.github.ruediste.simpledi.InstanceRequest;
import com.github.ruediste.simpledi.InstanceRequestEnricher;
import com.github.ruediste.simpledi.matchers.AbstractMatcher;
import com.github.ruediste.simpledi.matchers.Matcher;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {

	public static AttachedProperty<InstanceRequest<?>, Class<?>> keyAnnotationClass = new AttachedProperty<>(
			"keyAnnotationClass");
	public static AttachedProperty<InstanceRequest<?>, Annotation> keyAnnotation = new AttachedProperty<>(
			"keyAnnotation");

	public AnnotatedBindingBuilder(Matcher<InstanceRequest<?>> keyMatcher,
			InstanceRequest<?> eagerInstantiationKey, InjectorConfiguration config) {
		super(keyMatcher, eagerInstantiationKey, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> annotationType) {
		config.addKeyEnricher(new InstanceRequestEnricher() {

			@Override
			public void enrich(InstanceRequest<?> key, InjectionPoint injectionPoint) {
				if (keyMatcher.matches(key)
						&& injectionPoint.getAnnotated().isAnnotationPresent(
								annotationType)) {
					keyAnnotationClass.set(key, annotationType);
				}
			}
		});

		InstanceRequest<?> newKey = new InstanceRequest<>(eagerInstantiationKey);
		keyAnnotationClass.set(newKey, annotationType);

		return new LinkedBindingBuilder<>(
				keyMatcher.and(new AbstractMatcher<InstanceRequest<?>>() {

					@Override
					public boolean matches(InstanceRequest<?> t) {
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
			public void enrich(InstanceRequest<?> key, InjectionPoint injectionPoint) {
				Annotation present = injectionPoint.getAnnotated()
						.getAnnotation(annotation.getClass());
				if (keyMatcher.matches(key) && annotation.equals(present)) {
					keyAnnotation.set(key, present);
				}
			}
		});

		InstanceRequest<?> newKey = new InstanceRequest<>(eagerInstantiationKey);
		keyAnnotation.set(newKey, annotation);

		return new LinkedBindingBuilder<>(
				keyMatcher.and(new AbstractMatcher<InstanceRequest<?>>() {

					@Override
					public boolean matches(InstanceRequest<?> t) {
						return annotation.equals(keyAnnotation.get(t));
					}
				}), newKey, config);
	}
}

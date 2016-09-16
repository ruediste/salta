package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

/**
 * See the EDSL examples at {@link StandardBinder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface StandardAnnotatedBindingBuilder<T> extends StandardLinkedBindingBuilder<T> {

    /**
     * See the EDSL examples at {@link StandardBinder}.
     */
    StandardLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> availableAnnotationType);

    /**
     * See the EDSL examples at {@link StandardBinder}.
     */
    StandardLinkedBindingBuilder<T> annotatedWith(Annotation availableAnnotation);
}

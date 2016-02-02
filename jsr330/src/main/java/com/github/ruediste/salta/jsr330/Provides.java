package com.github.ruediste.salta.jsr330;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates methods of a {@link SaltaModule} to create a provider method
 * binding. The method's return type is bound to its returned value. Salta will
 * pass dependencies to the method as parameters.
 *
 * <p>
 * It is possible to override {@code @Provides} methods in subclasses of a
 * module. In this case, the overridden method (in the base class) is completely
 * ignored. This implies that the {@code @Provides} annotation has to be
 * repeated in the sub class for the binding to occur. In particular, this can
 * be used to disable a provides method of the base class.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Provides {

}

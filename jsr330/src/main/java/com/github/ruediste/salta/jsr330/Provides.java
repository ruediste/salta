package com.github.ruediste.salta.jsr330;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.ruediste.salta.standard.SaltaModule;

/**
 * Annotates methods of a {@link SaltaModule} to create a provider method binding.
 * The method's return type is bound to its returned value. Salta will pass
 * dependencies to the method as parameters.
 *
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Provides {

}

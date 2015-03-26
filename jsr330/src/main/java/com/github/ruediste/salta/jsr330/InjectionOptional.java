package com.github.ruediste.salta.jsr330;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;

/**
 * Indicates an optional injection point.
 * <p>
 * It can be used on fields, methods as well as on parameters of constructors
 * and methods annotated with {@link Inject @Inject}
 */
@Documented
@Retention(RUNTIME)
@Target({ METHOD, FIELD, ElementType.PARAMETER })
public @interface InjectionOptional {

}

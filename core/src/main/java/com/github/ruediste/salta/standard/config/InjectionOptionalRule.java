package com.github.ruediste.salta.standard.config;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Function;

/**
 * Rule to determine if the injection of a {@link Field}, {@link Method} or
 * {@link Parameter} is optional or mandatory
 */
public interface InjectionOptionalRule extends
		Function<AnnotatedElement, Optional<Boolean>> {

}

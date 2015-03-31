package com.github.ruediste.salta.jsr330.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRule;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.standard.InjectionPoint;

/**
 * Creation rule for injecting {@link Logger}s initialized named by the class
 * they are injected into.
 */
public class LoggerCreationRule implements CreationRule {

	private Class<?> loggerClass;
	private Function<Class<?>, Object> loggerFactory;

	public LoggerCreationRule(Class<?> loggerClass,
			Function<Class<?>, Object> loggerFactory) {
		this.loggerClass = loggerClass;
		this.loggerFactory = loggerFactory;

	}

	@Override
	public Optional<Function<RecipeCreationContext, SupplierRecipe>> apply(
			CoreDependencyKey<?> key) {

		if (key instanceof InjectionPoint
				&& loggerClass.equals(key.getRawType())) {
			Class<?> declaringClass = ((InjectionPoint<?>) key).getMember()
					.getDeclaringClass();
			return Optional.of(ctx -> new SupplierRecipeImpl(
					() -> loggerFactory.apply(declaringClass)));
		}
		return Optional.empty();
	}
}
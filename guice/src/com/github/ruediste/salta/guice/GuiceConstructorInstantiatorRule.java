package com.github.ruediste.salta.guice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

public class GuiceConstructorInstantiatorRule extends
		ConstructorInstantiatorRuleBase {

	@Override
	public RecipeInstantiator apply(RecipeCreationContext ctx,
			TypeToken<?> typeToken) {
		if (TypeLiteral.class.equals(typeToken.getType())) {
			throw new ProvisionException(
					"Cannot inject a TypeLiteral that has no type parameter");
		}

		return super.apply(ctx, typeToken);
	}

	@Override
	protected Integer getConstructorPriority(Constructor<?> c) {
		if (c.isAnnotationPresent(Inject.class)
				|| c.isAnnotationPresent(javax.inject.Inject.class))
			return 2;
		boolean isInnerClass = c.getDeclaringClass().getEnclosingClass() != null;

		if (c.getParameterCount() == 0
				&& (Modifier.isPublic(c.getModifiers()) || isInnerClass))
			return 1;
		return null;
	}

}

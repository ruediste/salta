package com.github.ruediste.salta.guice;

import java.lang.reflect.Constructor;

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
		if (c.isAnnotationPresent(Inject.class))
			return 2;
		if (c.getParameterCount() == 0)
			return 1;
		return null;
	}

	@Override
	protected ProvisionException noConstructorFound(TypeToken<?> typeToken,
			Class<?> clazz) {
		Class<?> enclosingClass = clazz.getEnclosingClass();
		if (enclosingClass != null) {
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				if (c.getParameterCount() == 1
						&& enclosingClass.equals(c.getParameterTypes()[0])) {
					return new ProvisionException(
							"No suitable constructor found for inner non-static class "
									+ typeToken
									+ ".\nCannot instantiate non-static inner classes. Forgotten to make class static?");
				}
			}
		}
		return super.noConstructorFound(typeToken, clazz);
	}
}

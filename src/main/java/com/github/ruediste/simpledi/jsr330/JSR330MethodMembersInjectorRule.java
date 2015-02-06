package com.github.ruediste.simpledi.jsr330;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.inject.Inject;

import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.standard.recipe.StandardCreationRecipe;
import com.github.ruediste.simpledi.standard.util.MethodMembersInjectorRuleBase;
import com.github.ruediste.simpledi.standard.util.MethodOverrideIndex;
import com.google.common.reflect.TypeToken;

public class JSR330MethodMembersInjectorRule extends
		MethodMembersInjectorRuleBase {

	@Override
	public void addMembersInjectors(TypeToken<?> typeToken,
			StandardCreationRecipe recipe) {
		super.addMembersInjectors(typeToken, recipe);
	}

	@Override
	protected boolean isInjectableMethod(TypeToken<?> declaringType,
			Method method, MethodOverrideIndex index) {
		if (Modifier.isAbstract(method.getModifiers()))
			return false;
		if (method.getTypeParameters().length > 0) {
			throw new ProvisionException(
					"Method is annotated with @Inject but declares type parameters. Method:\n"
							+ method);
		}
		if (index.isOverridden(method))
			return false;
		return method.isAnnotationPresent(Inject.class);
	}

}

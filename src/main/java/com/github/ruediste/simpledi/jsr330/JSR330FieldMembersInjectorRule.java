package com.github.ruediste.simpledi.jsr330;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.inject.Inject;

import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.standard.util.FieldMembersInjectorRuleBase;
import com.google.common.reflect.TypeToken;

public class JSR330FieldMembersInjectorRule extends
		FieldMembersInjectorRuleBase {

	@Override
	protected boolean isInjectableField(TypeToken<?> declaringType, Field f) {
		boolean annotationPresent = f.isAnnotationPresent(Inject.class);
		if (annotationPresent && Modifier.isFinal(f.getModifiers())) {
			throw new ProvisionException("Final field annotated with @Inject");
		}
		return annotationPresent;
	}

}

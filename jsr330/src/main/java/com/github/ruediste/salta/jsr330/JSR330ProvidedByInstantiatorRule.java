package com.github.ruediste.salta.jsr330;

import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.binder.InstanceProvider;
import com.github.ruediste.salta.standard.util.ProvidedByConstructionRuleBase;
import com.google.common.reflect.TypeToken;

public class JSR330ProvidedByInstantiatorRule extends
		ProvidedByConstructionRuleBase {

	public JSR330ProvidedByInstantiatorRule() {
		super(InstanceProvider.class);
	}

	/**
	 * Get the provider key to be used
	 */
	@Override
	protected DependencyKey<?> getProviderKey(TypeToken<?> type) {
		ProvidedBy providedBy = type.getRawType().getAnnotation(
				ProvidedBy.class);

		if (providedBy != null)
			return DependencyKey.of(providedBy.value());
		else
			return null;
	}

}

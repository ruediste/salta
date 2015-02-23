package com.github.ruediste.salta.jsr330;

import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.util.ImplementedByInstantiatorRuleBase;
import com.google.common.reflect.TypeToken;

public class JSR330ImplementedByInstantiatorRule extends
		ImplementedByInstantiatorRuleBase {

	@Override
	protected DependencyKey<?> getImplementorKey(TypeToken<?> type) {
		ImplementedBy implementedBy = type.getRawType().getAnnotation(
				ImplementedBy.class);

		if (implementedBy != null)
			return DependencyKey.of(implementedBy.value());
		else
			return null;
	}

}

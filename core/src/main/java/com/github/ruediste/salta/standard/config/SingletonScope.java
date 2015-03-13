package com.github.ruediste.salta.standard.config;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.util.Accessibility;
import com.google.common.reflect.TypeToken;

public class SingletonScope implements Scope {

	private static AttachedProperty<Binding, Object> instance = new AttachedProperty<>(
			"singleton instance");

	@Override
	public String toString() {
		return "Singleton";
	}

	@Override
	public SupplierRecipe createRecipe(RecipeCreationContext ctx,
			Binding binding, TypeToken<?> requestedType,
			SupplierRecipe innerRecipe) {
		if (!instance.isSet(binding)) {
			ctx.queueAction(() -> {
				if (!instance.isSet(binding)) {
					instance.set(binding,
							ctx.getCompiler().compileSupplier(innerRecipe)
									.getNoThrow());
				}
			});
		}
		return new SupplierRecipe() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext ctx) {

				Class<?> fieldType = requestedType.getRawType();
				if (!Accessibility.isClassPublic(fieldType)) {
					fieldType = Object.class;
				}
				ctx.addFieldAndLoad((Class) fieldType, instance.get(binding));
				return fieldType;
			}
		};
	}

	public void instantiate(RecipeCreationContext ctx, Binding binding,
			SupplierRecipe innerRecipe) {
		if (!instance.isSet(binding)) {
			instance.set(binding, ctx.getCompiler()
					.compileSupplier(innerRecipe).getNoThrow());
		}
	}
}
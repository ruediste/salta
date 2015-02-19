package com.github.ruediste.salta.core;

import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class CreationRecipeImpl extends CreationRecipe {

	private Supplier<Object> supplier;

	public CreationRecipeImpl(Supplier<Object> supplier) {
		this.supplier = supplier;
	}

	@Override
	public void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {
		compilationContext.addFieldAndLoad(Supplier.class, supplier);
		mv.invokeInterface(Type.getType(Supplier.class),
				Method.getMethod("Object get()"));
	}

}

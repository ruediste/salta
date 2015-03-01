package com.github.ruediste.salta.core.compile;

import java.util.function.Supplier;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Implementation of {@link SupplierRecipe} using a {@link Supplier} to generate
 * the result
 */
public class SupplierRecipeImpl extends SupplierRecipe {

	private Supplier<Object> supplier;

	public SupplierRecipeImpl(Supplier<Object> supplier) {
		this.supplier = supplier;
	}

	@Override
	protected Class<?> compileImpl(GeneratorAdapter mv,
			MethodCompilationContext ctx) {
		ctx.addFieldAndLoad(Supplier.class, supplier);
		mv.invokeInterface(Type.getType(Supplier.class),
				Method.getMethod("Object get()"));

		return Object.class;
	}

}

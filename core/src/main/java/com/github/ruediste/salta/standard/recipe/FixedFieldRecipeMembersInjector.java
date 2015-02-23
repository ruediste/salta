package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.Field;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.SupplierRecipe;

public class FixedFieldRecipeMembersInjector extends RecipeMembersInjector {

	private Field field;
	private SupplierRecipe recipe;

	public FixedFieldRecipeMembersInjector(Field field, SupplierRecipe recipe) {
		this.field = field;
		this.recipe = recipe;
		field.setAccessible(true);

	}

	@Override
	public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {
		mv.dup();
		compilationContext.addFieldAndLoad(Type.getDescriptor(Field.class),
				field);
		mv.swap();
		Class<?> t = recipe.compile(compilationContext);
		if (t.isPrimitive())
			mv.box(Type.getType(t));
		mv.invokeVirtual(Type.getType(Field.class), new Method("set",
				"(Ljava/lang/Object;Ljava/lang/Object;)V"));
		return argType;
	}
}

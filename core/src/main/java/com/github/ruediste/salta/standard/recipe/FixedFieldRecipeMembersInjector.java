package com.github.ruediste.salta.standard.recipe;

import java.lang.reflect.Field;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;

public class FixedFieldRecipeMembersInjector implements RecipeMembersInjector {

	private Field field;
	private CreationRecipe recipe;

	public FixedFieldRecipeMembersInjector(Field field, CreationRecipe recipe) {
		this.field = field;
		this.recipe = recipe;
		field.setAccessible(true);

	}

	@Override
	public void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {
		mv.dup();
		compilationContext.addAndLoad(Type.getDescriptor(Field.class), field);
		mv.swap();
		recipe.compile(mv, compilationContext);

		mv.invokeVirtual(Type.getType(Field.class), new Method("set",
				"(Ljava/lang/Object;Ljava/lang/Object;)V"));

	}
}

package com.github.ruediste.salta.standard.recipe;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ANEWARRAY;

import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.RecipeCompilationContext;

public class FixedMethodRecipeMembersInjector implements RecipeMembersInjector {

	private Method method;
	private List<CreationRecipe> argumentRecipes;

	public FixedMethodRecipeMembersInjector(Method method,
			List<CreationRecipe> argumentRecipes) {
		this.method = method;
		this.argumentRecipes = argumentRecipes;
		method.setAccessible(true);
	}

	@Override
	public void compile(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {

		mv.dup();
		compilationContext.addAndLoad(Type.getDescriptor(Method.class), method);
		mv.swap();

		// push dependencies as an array
		mv.push(argumentRecipes.size());
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

		for (int i = 0; i < argumentRecipes.size(); i++) {
			mv.dup();
			mv.push(i);
			CreationRecipe dependency = argumentRecipes.get(i);
			dependency.compile(mv, compilationContext);
			mv.visitInsn(AASTORE);
		}

		mv.invokeVirtual(
				Type.getType(Method.class),
				new org.objectweb.asm.commons.Method("invoke",
						"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));
		mv.pop();
	}

}

package com.github.ruediste.salta.standard.recipe;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.SupplierRecipe;
import com.github.ruediste.salta.standard.util.Accessibility;

public class FixedMethodRecipeMembersInjector extends RecipeMembersInjector {

	private Method method;
	private List<SupplierRecipe> argumentRecipes;

	public FixedMethodRecipeMembersInjector(Method method,
			List<SupplierRecipe> argumentRecipes) {
		this.method = method;
		this.argumentRecipes = argumentRecipes;
		method.setAccessible(true);
	}

	@Override
	public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {

		if (Accessibility.isMethodPublic(method))
			return compileDirect(argType, mv, compilationContext);
		return compileReflection(argType, mv, compilationContext);
	}

	private Class<?> compileDirect(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {
		// check type
		Class<?> declaringClass = method.getDeclaringClass();

		if (!declaringClass.isAssignableFrom(argType)) {
			compilationContext.cast(argType, declaringClass);

			argType = declaringClass;
		}
		mv.dup();
		// push dependencies as an array

		for (int i = 0; i < argumentRecipes.size(); i++) {
			SupplierRecipe dependency = argumentRecipes.get(i);
			Class<?> t = dependency.compile(compilationContext);
			Class<?> parameterType = method.getParameterTypes()[i];

			if (!parameterType.isAssignableFrom(t)) {
				if (!parameterType.isPrimitive() && t.isPrimitive()) {
					// mv.box(Type.getType(t));
				}
			}
			if (t.isPrimitive())
				// mv.box(Type.getType(t));
				;
		}

		if (declaringClass.isInterface())
			mv.invokeInterface(Type.getType(declaringClass),
					org.objectweb.asm.commons.Method.getMethod(method));
		else
			mv.invokeVirtual(Type.getType(declaringClass),
					org.objectweb.asm.commons.Method.getMethod(method));

		if (!void.class.equals(method.getReturnType())) {
			mv.pop();
		}
		return argType;
	}

	private Class<?> compileReflection(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {
		mv.dup();
		compilationContext.addFieldAndLoad(Type.getDescriptor(Method.class),
				method);
		mv.swap();

		// push dependencies as an array
		mv.push(argumentRecipes.size());
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

		for (int i = 0; i < argumentRecipes.size(); i++) {
			mv.dup();
			mv.push(i);
			SupplierRecipe dependency = argumentRecipes.get(i);
			Class<?> t = dependency.compile(compilationContext);
			if (t.isPrimitive())
				mv.box(Type.getType(t));
			mv.visitInsn(AASTORE);
		}

		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l1,
				Type.getInternalName(InvocationTargetException.class));
		mv.visitLabel(l0);
		mv.invokeVirtual(
				Type.getType(Method.class),
				new org.objectweb.asm.commons.Method("invoke",
						"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));
		mv.goTo(l2);
		mv.visitLabel(l1);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				Type.getInternalName(InvocationTargetException.class),
				"getCause", "()Ljava/lang/Throwable;", false);
		mv.visitInsn(ATHROW);
		mv.visitLabel(l2);

		mv.pop();
		return argType;
	}

}

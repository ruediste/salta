package com.github.ruediste.salta.standard.recipe;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.lang.invoke.ConstantCallSite;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.compile.FunctionRecipe;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.MethodRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.standard.util.Accessibility;

public class FixedMethodInvocationFunctionRecipe implements FunctionRecipe {

	private Method method;
	private List<SupplierRecipe> argumentRecipes;
	private InjectionStrategy injectionStrategy;

	public FixedMethodInvocationFunctionRecipe(Method method,
			List<SupplierRecipe> argumentRecipes,
			InjectionStrategy injectionStrategy) {
		this.method = method;
		this.argumentRecipes = argumentRecipes;
		this.injectionStrategy = injectionStrategy;
		method.setAccessible(true);
	}

	@Override
	public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext ctx) {

		if (Accessibility.isMethodPublic(method))
			return compileDirect(argType, mv, ctx);
		else
			switch (injectionStrategy) {
			case INVOKE_DYNAMIC:
				return compileDynamic(argType, mv, ctx);
			case REFLECTION:
				return compileReflection(argType, mv, ctx);
			default:
				throw new UnsupportedOperationException();
			}
	}

	private Class<?> compileDirect(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext ctx) {

		// cast receiver
		argType = ctx.castToPublic(argType, method.getDeclaringClass());

		// push dependencies as an array
		for (int i = 0; i < argumentRecipes.size(); i++) {
			Class<?> t = argumentRecipes.get(i).compile(ctx);
			ctx.castToPublic(t, method.getParameterTypes()[i]);
		}

		if (method.getDeclaringClass().isInterface())
			mv.invokeInterface(Type.getType(method.getDeclaringClass()),
					org.objectweb.asm.commons.Method.getMethod(method));
		else
			mv.invokeVirtual(Type.getType(method.getDeclaringClass()),
					org.objectweb.asm.commons.Method.getMethod(method));

		return ctx.castToPublic(method.getReturnType(), method.getReturnType());
	}

	private Class<?> compileReflection(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext compilationContext) {
		compilationContext.addFieldAndLoad(Method.class, method);
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

		if (void.class.equals(method.getReturnType())) {
			mv.pop();
			return void.class;
		}
		return Object.class;
	}

	private Class<?> compileDynamic(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext ctx) {
		// cast receiver
		Type[] argTypes = new Type[argumentRecipes.size() + 1];
		argType = ctx.castToPublic(argType, method.getDeclaringClass());
		argTypes[0] = Type.getType(argType);

		// push arguments
		for (int i = 0; i < argumentRecipes.size(); i++) {
			Class<?> t = argumentRecipes.get(i).compile(ctx);
			argTypes[i + 1] = Type.getType(ctx.castToPublic(t,
					method.getParameterTypes()[i]));
		}

		String bootstrapDesc = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";
		String bootstrapName = ctx.getClassCtx().addMethod(
				ACC_PRIVATE + ACC_STATIC, bootstrapDesc, null,
				new MethodRecipe() {

					@Override
					protected void compileImpl(GeneratorAdapter mv,
							MethodCompilationContext ctx) {
						mv.newInstance(Type.getType(ConstantCallSite.class));
						mv.dup();

						mv.loadArg(0);
						ctx.addFieldAndLoad(Method.class, method);

						mv.visitMethodInsn(
								INVOKEVIRTUAL,
								"java/lang/invoke/MethodHandles$Lookup",
								"unreflect",
								"(Ljava/lang/reflect/Method;)Ljava/lang/invoke/MethodHandle;",
								false);
						mv.loadArg(2);
						mv.visitMethodInsn(
								INVOKEVIRTUAL,
								"java/lang/invoke/MethodHandle",
								"asType",
								"(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
								false);
						mv.visitMethodInsn(INVOKESPECIAL,
								"java/lang/invoke/ConstantCallSite", "<init>",
								"(Ljava/lang/invoke/MethodHandle;)V", false);
						mv.visitInsn(ARETURN);
					}
				});

		// call
		Handle bsm = new Handle(H_INVOKESTATIC, ctx.getClassCtx()
				.getInternalClassName(), bootstrapName, bootstrapDesc);

		Class<?> returnType = ctx.publicSuperType(method.getReturnType());

		mv.invokeDynamic(method.getName(),
				Type.getMethodDescriptor(Type.getType(returnType), argTypes),
				bsm);

		return returnType;
	}

}

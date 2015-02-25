package com.github.ruediste.salta.standard.recipe;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.SupplierRecipe;
import com.github.ruediste.salta.standard.util.Accessibility;

public class FixedMethodRecipeMembersInjector extends RecipeMembersInjector {

	private Method method;
	private List<SupplierRecipe> argumentRecipes;
	private static Lookup lookup;
	private InjectionStrategy injectionStrategy;

	static {
		try {
			Field f = MethodHandles.Lookup.class
					.getDeclaredField("IMPL_LOOKUP");
			f.setAccessible(true);
			lookup = (Lookup) f.get(null);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			throw new SaltaException("Error while retrieving unbound lookup");
		}
	}

	public FixedMethodRecipeMembersInjector(Method method,
			List<SupplierRecipe> argumentRecipes,
			InjectionStrategy injectionStrategy) {
		this.method = method;
		this.argumentRecipes = argumentRecipes;
		this.injectionStrategy = injectionStrategy;
		method.setAccessible(true);

	}

	@Override
	public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {

		if (Accessibility.isMethodPublic(method))
			return compileDirect(argType, mv, compilationContext);
		else
			switch (injectionStrategy) {
			case INVOKE_DYNAMIC:
				return compileDynamic(argType, mv, compilationContext);
			case METHOD_HANDLES:
				return compileMethodHandles(argType, mv, compilationContext);
			case REFLECTION:
				return compileReflection(argType, mv, compilationContext);
			default:
				throw new UnsupportedOperationException();
			}
	}

	private Class<?> compileDirect(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext ctx) {
		// cast receiver
		Class<?> declaringClass = method.getDeclaringClass();
		if (!declaringClass.isAssignableFrom(argType))
			mv.checkCast(Type.getType(declaringClass));
		argType = declaringClass;

		mv.dup();

		// push dependencies as an array
		for (int i = 0; i < argumentRecipes.size(); i++) {
			SupplierRecipe dependency = argumentRecipes.get(i);
			Class<?> t = dependency.compile(ctx);
			ctx.cast(t, method.getParameterTypes()[i]);
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

	private Class<?> compileMethodHandles(Class<?> argType,
			GeneratorAdapter mv, RecipeCompilationContext ctx) {
		mv.dup();
		MethodHandle handle;
		try {
			handle = lookup.unreflect(method);
		} catch (IllegalAccessException e) {
			throw new SaltaException(e);
		}
		ctx.addFieldAndLoad(Type.getDescriptor(MethodHandle.class), handle);

		mv.swap();

		Type[] argTypes = new Type[argumentRecipes.size() + 1];
		Class<?> declaringClass = method.getDeclaringClass();

		if (Accessibility.isClassPublic(declaringClass)) {
			if (!declaringClass.isAssignableFrom(argType)) {
				mv.checkCast(Type.getType(declaringClass));
				argType = declaringClass;
			}
			argTypes[0] = Type.getType(declaringClass);
		} else
			argTypes[0] = Type.getType(Object.class);

		for (int i = 0; i < argumentRecipes.size(); i++) {
			SupplierRecipe dependency = argumentRecipes.get(i);
			Class<?> paramType = method.getParameterTypes()[i];
			Class<?> t = dependency.compile(ctx);
			if (Accessibility.isClassPublic(paramType)) {
				ctx.cast(t, paramType);
				argTypes[i + 1] = Type.getType(paramType);
			} else
				argTypes[i + 1] = Type.getType(Object.class);
		}

		mv.invokeVirtual(
				Type.getType(MethodHandle.class),
				new org.objectweb.asm.commons.Method(
						"invoke",
						Type.getMethodDescriptor(
								void.class.equals(method.getReturnType()) ? Type.VOID_TYPE
										: Type.getType(Object.class), argTypes)));

		if (!void.class.equals(method.getReturnType()))
			mv.pop();
		return argType;
	}

	private Class<?> compileDynamic(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext ctx) {
		mv.dup();
		// push dependencies as an array
		Class<?> declaringClass = method.getDeclaringClass();

		Type[] argTypes = new Type[argumentRecipes.size() + 1];
		if (Accessibility.isClassPublic(declaringClass)) {
			if (!declaringClass.isAssignableFrom(argType)) {
				mv.checkCast(Type.getType(declaringClass));
				argType = declaringClass;
			}
			argTypes[0] = Type.getType(argType);
		} else
			argTypes[0] = Type.getType(Object.class);

		for (int i = 0; i < argumentRecipes.size(); i++) {
			SupplierRecipe dependency = argumentRecipes.get(i);
			Class<?> t = dependency.compile(ctx);
			Class<?> paramType = method.getParameterTypes()[i];
			if (Accessibility.isClassPublic(paramType)) {
				ctx.cast(t, paramType);
				argTypes[i + 1] = Type.getType(paramType);
			} else
				argTypes[i + 1] = Type.getType(Object.class);
		}
		Handle bsm = new Handle(
				H_INVOKESTATIC,
				Type.getInternalName(FixedMethodRecipeMembersInjector.class),
				"bootstrap",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/CallSite;");

		mv.invokeDynamic(method.getName(), Type.getMethodDescriptor(
				Type.getType(method.getReturnType()), argTypes), bsm,
				declaringClass.getName(), Type.getMethodDescriptor(method));
		if (!void.class.equals(method.getReturnType()))
			mv.pop();
		return argType;
	}

	public static CallSite bootstrap(MethodHandles.Lookup dummy, String name,
			MethodType stackType, String declaringClassName,
			String origDescriptor) throws Exception {
		ClassLoader loader = dummy.lookupClass().getClassLoader();
		Class<?> declaringClass = loader.loadClass(declaringClassName);
		MethodHandle method = lookup.findVirtual(declaringClass, name,
				MethodType.fromMethodDescriptorString(origDescriptor, loader));

		return new ConstantCallSite(method.asType(stackType));
	}
}

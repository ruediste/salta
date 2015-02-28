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
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SupplierRecipe;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.util.Accessibility;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;
import com.google.common.reflect.TypeToken;

/**
 * Instantiate a fixed class using a fixed constructor. Use a subclass of
 * {@link ConstructorInstantiatorRuleBase} to create an instance
 */
public class FixedConstructorRecipeInstantiator extends RecipeInstantiator {

	Constructor<?> constructor;
	List<SupplierRecipe> argumentDependencies;
	private InjectionStrategy strategy;

	public FixedConstructorRecipeInstantiator(Constructor<?> constructor,
			List<SupplierRecipe> argumentDependencies,
			InjectionStrategy strategy) {
		this.strategy = strategy;
		constructor.setAccessible(true);
		this.constructor = constructor;
		this.argumentDependencies = new ArrayList<>(argumentDependencies);
	}

	public static FixedConstructorRecipeInstantiator of(TypeToken<?> typeToken,
			RecipeCreationContext ctx, Constructor<?> constructor,
			InjectionStrategy strategy) {
		ArrayList<SupplierRecipe> args = new ArrayList<>();

		Parameter[] parameters = constructor.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			@SuppressWarnings({ "unchecked", "rawtypes" })
			CoreDependencyKey<Object> dependency = new InjectionPoint(
					typeToken.resolveType(parameter.getParameterizedType()),
					constructor, parameter, i);
			args.add(ctx.getRecipe(dependency));
		}
		return new FixedConstructorRecipeInstantiator(constructor, args,
				strategy);
	}

	@Override
	public Class<?> compileImpl(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {
		if (Accessibility.isConstructorPublic(constructor))
			return compileDirect(mv, compilationContext);
		else
			switch (strategy) {
			case INVOKE_DYNAMIC:
				return compileDynamic(mv, compilationContext);
			case REFLECTION:
				return compileReflection(mv, compilationContext);
			default:
				throw new UnsupportedOperationException();
			}
	}

	private Class<?> compileReflection(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {

		// push constructor
		compilationContext.addFieldAndLoad(
				Type.getDescriptor(Constructor.class), constructor);

		// push dependencies as an array
		mv.push(argumentDependencies.size());
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

		for (int i = 0; i < argumentDependencies.size(); i++) {
			mv.dup();
			mv.push(i);
			SupplierRecipe dependency = argumentDependencies.get(i);
			Class<?> argType = dependency.compile(compilationContext);
			if (argType.isPrimitive())
				mv.box(Type.getType(argType));
			mv.visitInsn(AASTORE);
		}

		// call constructor
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l1,
				Type.getInternalName(InvocationTargetException.class));
		mv.visitLabel(l0);
		mv.invokeVirtual(Type.getType(Constructor.class), new Method(
				"newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;"));
		mv.goTo(l2);
		mv.visitLabel(l1);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				Type.getInternalName(InvocationTargetException.class),
				"getCause", "()Ljava/lang/Throwable;", false);
		mv.visitInsn(ATHROW);
		mv.visitLabel(l2);

		return Object.class;
	}

	private Class<?> compileDirect(GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {

		mv.newInstance(Type.getType(constructor.getDeclaringClass()));
		mv.dup();

		// push dependencies

		for (int i = 0; i < argumentDependencies.size(); i++) {
			SupplierRecipe dependency = argumentDependencies.get(i);
			Class<?> tosType = dependency.compile(compilationContext);
			Class<?> argType = constructor.getParameterTypes()[i];
			if (!argType.isAssignableFrom(tosType)) {
				if (argType.isPrimitive())
					mv.unbox(Type.getType(argType));
				else
					mv.checkCast(Type.getType(argType));
			}
		}

		// call constructor
		mv.invokeConstructor(Type.getType(constructor.getDeclaringClass()),
				Method.getMethod(constructor));

		return constructor.getDeclaringClass();
	}

	private Class<?> compileDynamic(GeneratorAdapter mv,
			RecipeCompilationContext ctx) {

		Class<?> resultType = constructor.getDeclaringClass();
		if (!Accessibility.isClassPublic(resultType))
			resultType = Object.class;

		// push arguments
		Type[] argTypes = new Type[argumentDependencies.size()];
		for (int i = 0; i < argumentDependencies.size(); i++) {
			Class<?> t = argumentDependencies.get(i).compile(ctx);
			argTypes[i] = Type.getType(ctx.castToPublic(t,
					constructor.getParameterTypes()[i]));
		}

		Type[] origArgTypes = new Type[constructor.getParameterCount()];
		for (int i = 0; i < constructor.getParameterCount(); i++) {
			origArgTypes[i] = Type.getType(constructor.getParameterTypes()[i]);
		}

		// call
		Handle bsm = new Handle(
				H_INVOKESTATIC,
				Type.getInternalName(getClass()),
				"bootstrap",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/CallSite;");
		mv.invokeDynamic("init", Type.getMethodDescriptor(
				Type.getType(resultType), argTypes), bsm, constructor
				.getDeclaringClass().getName(), Type.getMethodDescriptor(
				Type.getType(void.class), origArgTypes));

		return resultType;
	}

	public static CallSite bootstrap(MethodHandles.Lookup dummy, String name,
			MethodType stackType, String declaringClassName,
			String origDescriptor) throws Exception {
		ClassLoader loader = dummy.lookupClass().getClassLoader();
		Class<?> declaringClass = loader.loadClass(declaringClassName);
		MethodHandle method = UnrestrictedLookupHolder.lookup.findConstructor(
				declaringClass,
				MethodType.fromMethodDescriptorString(origDescriptor, loader));

		return new ConstantCallSite(method.asType(stackType));
	}
}

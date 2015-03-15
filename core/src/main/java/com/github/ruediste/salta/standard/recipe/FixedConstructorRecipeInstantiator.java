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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.MethodRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
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

	public static Optional<RecipeInstantiator> of(TypeToken<?> typeToken,
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
			Optional<SupplierRecipe> recipe = ctx.tryGetRecipe(dependency);
			if (!recipe.isPresent())
				return Optional.empty();
			args.add(recipe.get());
		}
		return Optional.of(new FixedConstructorRecipeInstantiator(constructor,
				args, strategy));
	}

	@Override
	public Class<?> compileImpl(GeneratorAdapter mv,
			MethodCompilationContext compilationContext) {
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
			MethodCompilationContext compilationContext) {

		// push constructor
		compilationContext.addFieldAndLoad(Constructor.class, constructor);

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
			MethodCompilationContext compilationContext) {

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
			MethodCompilationContext ctx) {

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
						ctx.addFieldAndLoad(Constructor.class, constructor);

						mv.visitMethodInsn(
								INVOKEVIRTUAL,
								"java/lang/invoke/MethodHandles$Lookup",
								"unreflectConstructor",
								"(Ljava/lang/reflect/Constructor;)Ljava/lang/invoke/MethodHandle;",
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
		mv.invokeDynamic("init",
				Type.getMethodDescriptor(Type.getType(resultType), argTypes),
				bsm);

		return resultType;
	}

}

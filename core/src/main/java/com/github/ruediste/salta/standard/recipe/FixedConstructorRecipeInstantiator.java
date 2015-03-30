package com.github.ruediste.salta.standard.recipe;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.util.Accessibility;
import com.github.ruediste.salta.standard.util.ConstructorInstantiatorRuleBase;
import com.google.common.base.Defaults;
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
			InjectionStrategy strategy, Predicate<Parameter> isOptional) {
		ArrayList<SupplierRecipe> args = new ArrayList<>();

		Parameter[] parameters = constructor.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			@SuppressWarnings({ "unchecked", "rawtypes" })
			CoreDependencyKey<Object> dependency = new InjectionPoint(
					typeToken.resolveType(parameter.getParameterizedType()),
					constructor, parameter, i);
			Optional<SupplierRecipe> argRecipe = ctx.tryGetRecipe(dependency);
			if (argRecipe.isPresent())
				args.add(argRecipe.get());
			else {
				if (isOptional.test(parameter)) {
					args.add(new SupplierRecipeImpl(() -> Defaults
							.defaultValue(parameter.getType())));
				} else {
					throw new SaltaException(
							"Cannot resolve constructor parameter of "
									+ constructor + ":\n" + parameter);
				}
			}
		}
		return new FixedConstructorRecipeInstantiator(constructor, args,
				strategy);
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

		String bootstrapDesc = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;";

		// call
		Handle bsm = new Handle(H_INVOKESTATIC,
				Type.getInternalName(FixedConstructorRecipeInstantiator.class),
				"bootstrap", bootstrapDesc);
		mv.invokeDynamic("init",
				Type.getMethodDescriptor(Type.getType(resultType), argTypes),
				bsm, ctx.addField(Constructor.class, constructor).getName());

		return resultType;
	}

	public static CallSite bootstrap(Lookup lookup, String methodName,
			MethodType type, String constructorFieldName) throws Exception {
		Field field = lookup.lookupClass().getField(constructorFieldName);
		field.setAccessible(true);
		Constructor<?> constructor = (Constructor<?>) field.get(null);
		MethodHandle handle = lookup.unreflectConstructor(constructor);
		return new ConstantCallSite(handle.asType(type));
	}
}

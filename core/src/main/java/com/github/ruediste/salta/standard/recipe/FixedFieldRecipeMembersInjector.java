package com.github.ruediste.salta.standard.recipe;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.SupplierRecipe;
import com.github.ruediste.salta.standard.util.Accessibility;

public class FixedFieldRecipeMembersInjector extends RecipeMembersInjector {

	private Field field;
	private SupplierRecipe recipe;
	private InjectionStrategy strategy;

	public FixedFieldRecipeMembersInjector(Field field, SupplierRecipe recipe,
			InjectionStrategy strategy) {
		this.field = field;
		this.recipe = recipe;
		this.strategy = strategy;
		field.setAccessible(true);

	}

	@Override
	public Class<?> compileImpl(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext compilationContext) {
		if (Accessibility.isFieldPublic(field)) {
			return compileReflection(argType, mv, compilationContext);
		}
		switch (strategy) {
		case INVOKE_DYNAMIC:
		case METHOD_HANDLES:
			return compileDynamic(argType, mv, compilationContext);
		case REFLECTION:
			return compileReflection(argType, mv, compilationContext);
		default:
			throw new UnsupportedOperationException();
		}
	}

	protected Class<?> compileReflection(Class<?> argType, GeneratorAdapter mv,
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

	protected Class<?> compileDynamic(Class<?> argType, GeneratorAdapter mv,
			RecipeCompilationContext ctx) {

		// cast receiver
		argType = ctx.castToPublic(argType, field.getDeclaringClass());
		mv.dup();

		// push value
		Type valueType;
		{
			Class<?> t = recipe.compile(ctx);
			valueType = Type.getType(ctx.castToPublic(t, field.getType()));
		}

		// set field
		Handle bsm = new Handle(
				H_INVOKESTATIC,
				Type.getInternalName(getClass()),
				"bootstrap",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;");

		mv.invokeDynamic(field.getName(), Type.getMethodDescriptor(
				Type.getType(void.class), Type.getType(argType), valueType),
				bsm, field.getDeclaringClass().getName());
		return argType;
	}

	public static CallSite bootstrap(MethodHandles.Lookup dummy, String name,
			MethodType stackType, String declaringClassName) throws Exception {
		ClassLoader loader = dummy.lookupClass().getClassLoader();

		Class<?> declaringClass = loader.loadClass(declaringClassName);

		MethodHandle method = UnrestrictedLookupHolder.lookup
				.unreflectSetter(declaringClass.getDeclaredField(name));

		return new ConstantCallSite(method.asType(stackType));
	}
}

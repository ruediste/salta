package com.github.ruediste.salta.standard.recipe;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.lang.invoke.ConstantCallSite;
import java.lang.reflect.Field;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.InjectionStrategy;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.MethodRecipe;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
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
			MethodCompilationContext compilationContext) {
		if (Accessibility.isFieldPublic(field)) {
			return compileDirect(argType, mv, compilationContext);
		}
		switch (strategy) {
		case INVOKE_DYNAMIC:
			return compileDynamic(argType, mv, compilationContext);
		case REFLECTION:
			return compileReflection(argType, mv, compilationContext);
		default:
			throw new UnsupportedOperationException();
		}
	}

	protected Class<?> compileDirect(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext ctx) {
		argType = ctx.castToPublic(argType, field.getDeclaringClass());
		mv.dup();
		{
			Class<?> t = recipe.compile(ctx);
			ctx.castToPublic(t, field.getType());
		}

		mv.putField(Type.getType(field.getDeclaringClass()), field.getName(),
				Type.getType(field.getType()));

		return argType;
	}

	protected Class<?> compileReflection(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext compilationContext) {
		mv.dup();
		compilationContext.addFieldAndLoad(Field.class, field);
		mv.swap();
		Class<?> t = recipe.compile(compilationContext);
		if (t.isPrimitive())
			mv.box(Type.getType(t));
		mv.invokeVirtual(Type.getType(Field.class), new Method("set",
				"(Ljava/lang/Object;Ljava/lang/Object;)V"));
		return argType;
	}

	protected Class<?> compileDynamic(Class<?> argType, GeneratorAdapter mv,
			MethodCompilationContext ctx) {

		// cast receiver
		argType = ctx.castToPublic(argType, field.getDeclaringClass());
		mv.dup();

		// push value
		Type valueType;
		{
			Class<?> t = recipe.compile(ctx);
			valueType = Type.getType(ctx.castToPublic(t, field.getType()));
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
						ctx.addFieldAndLoad(Field.class, field);

						mv.visitMethodInsn(
								INVOKEVIRTUAL,
								"java/lang/invoke/MethodHandles$Lookup",
								"unreflectSetter",
								"(Ljava/lang/reflect/Field;)Ljava/lang/invoke/MethodHandle;",
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

		// set field
		Handle bsm = new Handle(H_INVOKESTATIC, ctx.getClassCtx()
				.getInternalClassName(), bootstrapName, bootstrapDesc);

		mv.invokeDynamic(
				"field",
				Type.getMethodDescriptor(Type.getType(void.class),
						Type.getType(argType), valueType), bsm);
		return argType;
	}

}

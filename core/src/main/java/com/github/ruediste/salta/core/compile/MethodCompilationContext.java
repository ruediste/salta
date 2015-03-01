package com.github.ruediste.salta.core.compile;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.util.Accessibility;

/**
 * Context for the compilation of a method. Provides various utility functions
 * and a link to the {@link ClassCompilationContext}
 */
public class MethodCompilationContext {

	private final ClassCompilationContext classCtx;
	private final GeneratorAdapter mv;

	public MethodCompilationContext(ClassCompilationContext classCtx,
			GeneratorAdapter mv) {
		this.classCtx = classCtx;
		this.mv = mv;
	}

	public GeneratorAdapter getMv() {
		return mv;
	}

	public <T> FieldHandle addFieldAndLoad(Class<T> fieldType, T value) {
		FieldHandle handle = getClassCtx().addField(fieldType, value);
		loadField(handle);
		return handle;
	}

	public void loadField(FieldHandle handle) {
		// getMv().loadThis();
		getMv().getStatic(Type.getObjectType(getClassCtx().getClazz().name),
				handle.name, Type.getType(handle.type));
	}

	private String desc(Class<?> returnType, Class<?>... parameterTypes) {
		Type[] params = new Type[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			params[i] = Type.getType(parameterTypes[i]);
		}
		return Type.getMethodDescriptor(Type.getType(returnType), params);
	}

	public void compileToSupplier(SupplierRecipe recipe) {
		String lambdaName;
		// generate lambda method
		{
			lambdaName = getClassCtx().addMethod(ACC_PRIVATE + ACC_SYNTHETIC,
					"()Ljava/lang/Object;", null, new MethodRecipe() {

						@Override
						protected void compileImpl(GeneratorAdapter mv,
								MethodCompilationContext ctx) {

							recipe.compile(ctx);

							mv.visitInsn(ARETURN);
						}
					});
		}
		getMv().visitVarInsn(ALOAD, 0);
		getMv().visitInvokeDynamicInsn(
				"get",
				Type.getMethodDescriptor(Type.getType(Supplier.class), Type
						.getObjectType(getClassCtx().getInternalClassName())),
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/invoke/LambdaMetafactory", "metafactory",
						desc(CallSite.class, MethodHandles.Lookup.class,
								String.class, MethodType.class,
								MethodType.class, MethodHandle.class,
								MethodType.class)),
				new Object[] {
						Type.getType("()Ljava/lang/Object;"),
						new Handle(Opcodes.H_INVOKESPECIAL, getClassCtx()
								.getInternalClassName(), lambdaName,
								"()Ljava/lang/Object;"),
						Type.getType("()Ljava/lang/Object;") });

	}

	public void pop(Class<?> type) {
		switch (Type.getType(type).getSize()) {
		case 0:
			break;
		case 1:
			getMv().pop();
			break;
		case 2:
			getMv().pop2();
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	public ClassCompilationContext getClassCtx() {
		return classCtx;
	}

	/**
	 * see {@link MethodCompilationContext#castToPublic(Class, Class)}
	 */
	public Class<?> castToPublic(Class<?> from, Class<?> to) {
		if (!Accessibility.isClassPublic(to))
			to = Object.class;

		if (from.equals(to))
			return to;

		if (from.isPrimitive() && to.isPrimitive()) {
			// two primitives which are not equal
			// fall throught to throw
		} else if (from.isPrimitive()) {
			if (!to.isArray() && !to.isPrimitive()) {
				// primitive to object
				mv.box(Type.getType(from));
				return to;
			}
		} else if (to.isPrimitive()) {
			if (!from.isArray()) {
				// any to primitive
				mv.unbox(Type.getType(to));
				return to;
			}
		} else {
			if (!to.isAssignableFrom(from)) {
				// downcast
				mv.checkCast(Type.getType(to));
			}
			return to;
		}
		throw new SaltaException("Cannot cast from " + from + " to " + to);
	}

}

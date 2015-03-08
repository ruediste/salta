package com.github.ruediste.salta.core.compile;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;

import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.standard.util.Accessibility;

/**
 * Context for the compilation of a method. Provides various utility functions
 * and a link to the {@link ClassCompilationContext}
 */
public class MethodCompilationContext {

	private final ClassCompilationContext classCtx;
	private final GeneratorAdapter mv;
	private int access;
	private String desc;

	public MethodCompilationContext(ClassCompilationContext classCtx,
			GeneratorAdapter mv, int access, String desc) {
		this.classCtx = classCtx;
		this.mv = mv;
		this.access = access;
		this.desc = desc;
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
		getMv().getStatic(
				Type.getObjectType(getClassCtx().getInternalClassName()),
				handle.name, Type.getType(handle.type));
	}

	private String desc(Class<?> returnType, Class<?>... parameterTypes) {
		Type[] params = new Type[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			params[i] = Type.getType(parameterTypes[i]);
		}
		return Type.getMethodDescriptor(Type.getType(returnType), params);
	}

	/**
	 * Compile the provided recipe and generate the code to push a
	 * {@link Supplier} instance to the stack. The supplier will call the code
	 * which has been compiled for the recipe.
	 */
	public void compileToSupplier(SupplierRecipe recipe) {
		String lambdaName;
		// generate lambda method
		{
			lambdaName = getClassCtx().addMethod(ACC_PRIVATE + ACC_SYNTHETIC,
					"()Ljava/lang/Object;", null, new MethodRecipe() {

						@Override
						protected void compileImpl(GeneratorAdapter mv,
								MethodCompilationContext ctx) {

							Class<?> t = recipe.compile(ctx);
							if (t.isPrimitive())
								mv.box(Type.getType(t));

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

	public Class<?> publicSuperType(Class<?> cls) {
		if (cls.isPrimitive())
			return cls;
		if (cls.isArray() || !Accessibility.isClassPublic(cls)) {
			return Object.class;
		}
		return cls;
	}

	private int codeSizeOffset;

	public void addCodeSizeOffset(int offset) {
		codeSizeOffset += offset;
	}

	public static class CodeSizeHelper {
		public MethodCompilationContext ctx;
		private CodeSizeEvaluator cse;

		public int getSize() {
			return cse.getMaxSize() + ctx.codeSizeOffset;
		}
	}

	public CodeSizeHelper getCodeSizeHelper() {
		ClassNode node = new ClassNode();
		node.name = getClassCtx().getClazz().name;
		ClassCompilationContext ccc = new ClassCompilationContext(node, true,
				classCtx.getCompiler());
		CodeSizeEvaluator cse = new CodeSizeEvaluator(null);
		MethodCompilationContext mcc = new MethodCompilationContext(ccc,
				new GeneratorAdapter(cse, access, "method", desc), access, desc);
		CodeSizeHelper helper = new CodeSizeHelper();
		helper.ctx = mcc;
		helper.cse = cse;
		return helper;
	}

	private boolean separateSubRecipes;

	public <T> T withSeparateSubRecipes(final boolean separateSubRecipes,
			Function<GeneratorAdapter, T> compileFunc) {
		boolean old = this.isSeparateSubRecipes();
		this.separateSubRecipes = separateSubRecipes;
		try {
			return compileFunc.apply(mv);
		} finally {
			this.separateSubRecipes = old;
		}
	}

	public boolean isSeparateSubRecipes() {
		return separateSubRecipes;
	}

}
package com.github.ruediste.salta.core;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.function.Supplier;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class RecipeCompilationContextBase implements
		RecipeCompilationContext {

	public ClassNode clazz;

	public static class FieldEntry {
		String name;
		Object value;
	}

	ArrayList<FieldEntry> fields = new ArrayList<>();
	ArrayList<Runnable> queuedActions = new ArrayList<>();
	private int methodNr;

	private final RecipeCompiler compiler;

	public RecipeCompilationContextBase(RecipeCompiler compiler) {
		this.compiler = compiler;

	}

	@Override
	public <T> FieldHandle addField(Class<T> fieldType, T value) {
		FieldEntry entry = new FieldEntry();
		entry.name = "field" + fields.size();
		entry.value = value;
		fields.add(entry);

		clazz.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, entry.name,
				Type.getDescriptor(fieldType), null, null);

		return new FieldHandle(fieldType, entry.name);
	}

	@Override
	public <T> FieldHandle addFieldAndLoad(Class<T> fieldType, T value) {
		FieldHandle handle = addField(fieldType, value);
		loadField(handle);
		return handle;
	}

	@Override
	public void loadField(FieldHandle handle) {
		// getMv().loadThis();
		getMv().getStatic(Type.getObjectType(clazz.name), handle.name,
				Type.getType(handle.type));
	}

	@Override
	public void queueAction(Runnable runnable) {
		queuedActions.add(runnable);
	}

	@Override
	public void compileToSupplier(SupplierRecipe recipe) {
		String lambdaName = "lambda$" + methodNr++;
		getMv().visitVarInsn(ALOAD, 0);
		getMv().visitInvokeDynamicInsn(
				"get",
				Type.getMethodDescriptor(Type.getType(Supplier.class),
						Type.getObjectType(clazz.name)),
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/invoke/LambdaMetafactory", "metafactory",
						desc(CallSite.class, MethodHandles.Lookup.class,
								String.class, MethodType.class,
								MethodType.class, MethodHandle.class,
								MethodType.class)),
				new Object[] {
						Type.getType("()Ljava/lang/Object;"),
						new Handle(Opcodes.H_INVOKESPECIAL, clazz.name,
								lambdaName, "()Ljava/lang/Object;"),
						Type.getType("()Ljava/lang/Object;") });

		{
			MethodNode m = new MethodNode(ACC_PRIVATE + ACC_SYNTHETIC,
					lambdaName, "()Ljava/lang/Object;", null, null);
			clazz.methods.add(m);
			GeneratorAdapter mv = new GeneratorAdapter(m.access, new Method(
					m.name, m.desc), m);
			mv.visitCode();

			RecipeCompilationContext innerContext = new RecipeCompilationContextBase(
					compiler) {

				@Override
				public GeneratorAdapter getMv() {
					return mv;
				}

			};
			recipe.compile(innerContext);

			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
	}

	private String desc(Class<?> returnType, Class<?>... parameterTypes) {
		Type[] params = new Type[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			params[i] = Type.getType(parameterTypes[i]);
		}
		return Type.getMethodDescriptor(Type.getType(returnType), params);
	}

	@Override
	public RecipeCompiler getCompiler() {
		return compiler;
	}

	@Override
	public Class<?> castToPublic(Class<?> from, Class<?> to) {
		return compiler.castToPublic(getMv(), from, to);
	}

	@Override
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
}

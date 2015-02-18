package com.github.ruediste.salta.core;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.function.Consumer;
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

	private CreationRecipeCompiler compiler;

	public RecipeCompilationContextBase(CreationRecipeCompiler compiler) {
		this.compiler = compiler;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.github.ruediste.salta.core.RecipeCompilationContext#addField(java
	 * .lang.String, java.lang.Object)
	 */
	@Override
	public String addField(String desc, Object value) {
		FieldEntry entry = new FieldEntry();
		entry.name = "field" + fields.size();
		entry.value = value;
		fields.add(entry);

		clazz.visitField(ACC_PUBLIC, entry.name, desc, null, null);

		return entry.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.github.ruediste.salta.core.RecipeCompilationContext#addFieldAndLoad
	 * (java.lang.String, java.lang.Object)
	 */
	@Override
	public String addFieldAndLoad(String desc, Object value) {
		String fieldName = addField(desc, value);
		getMv().loadThis();
		getMv().getField(Type.getObjectType(clazz.name), fieldName,
				Type.getType(desc));
		return fieldName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.github.ruediste.salta.core.RecipeCompilationContext#compileRecipe
	 * (com.github.ruediste.salta.core.CreationRecipe)
	 */
	@Override
	public CompiledCreationRecipe compileRecipe(CreationRecipe recipe) {
		return compiler.compile(recipe);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.github.ruediste.salta.core.RecipeCompilationContext#queueCompilation
	 * (com.github.ruediste.salta.core.CreationRecipe,
	 * java.util.function.Consumer)
	 */
	@Override
	public void queueCompilation(CreationRecipe recipe,
			Consumer<CompiledCreationRecipe> callback) {
		queuedActions.add(() -> {
			CompiledCreationRecipe compiledRecipe = compiler.compile(recipe);
			callback.accept(compiledRecipe);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.github.ruediste.salta.core.RecipeCompilationContext#compileToSupplier
	 * (com.github.ruediste.salta.core.CreationRecipe)
	 */
	@Override
	public void compileToSupplier(CreationRecipe recipe) {
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
			recipe.compile(mv, innerContext);

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

	public abstract GeneratorAdapter getMv();

}

package com.github.ruediste.salta.core;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;

public class RecipeCompilationContext {

	public ClassNode clazz;

	public static class FieldEntry {
		String name;
		Object value;
	}

	ArrayList<FieldEntry> fields = new ArrayList<>();
	ArrayList<Runnable> queuedActions = new ArrayList<>();

	public GeneratorAdapter mv;
	private CreationRecipeCompiler compiler;

	public RecipeCompilationContext(CreationRecipeCompiler compiler) {
		this.compiler = compiler;

	}

	/**
	 * Add a field to the generated method and return it's name. The field will
	 * be initialized to the given value
	 */
	public String addField(String desc, Object value) {
		FieldEntry entry = new FieldEntry();
		entry.name = "field" + fields.size();
		entry.value = value;
		fields.add(entry);

		clazz.visitField(ACC_PUBLIC, entry.name, desc, null, null);

		return entry.name;
	}

	public String addAndLoad(String desc, Object value) {
		String fieldName = addField(desc, value);
		mv.loadThis();
		mv.getField(Type.getObjectType(clazz.name), fieldName,
				Type.getType(desc));
		return fieldName;
	}

	public String getGeneratedClassName() {
		return clazz.name;
	}

	public CompiledCreationRecipe compileRecipe(CreationRecipe recipe) {
		return compiler.compile(recipe);
	}

	public void queueCompilation(CreationRecipe recipe,
			Consumer<CompiledCreationRecipe> callback) {
		queuedActions.add(() -> {
			CompiledCreationRecipe compiledRecipe = compiler.compile(recipe);
			callback.accept(compiledRecipe);
		});
	}
}

package com.github.ruediste.salta.core;

import java.util.function.Consumer;

public interface RecipeCompilationContext {

	/**
	 * Add a field to the generated method and return it's name. The field will
	 * be initialized to the given value
	 */
	public abstract String addField(String desc, Object value);

	public abstract <T> String addFieldAndLoad(Class<T> fieldType, T value);

	/**
	 * Add a field to the compiled recipe class and load it.
	 * 
	 * @param desc
	 *            descriptor of the field
	 * @param value
	 *            value the field is initialized to
	 * @return name of the added field
	 */
	public abstract String addFieldAndLoad(String desc, Object value);

	/**
	 * Compile a recipe
	 */
	public abstract CompiledCreationRecipe compileRecipe(CreationRecipe recipe);

	/**
	 * Queue the comilation of a recipe
	 */
	public abstract void queueCompilation(CreationRecipe recipe,
			Consumer<CompiledCreationRecipe> callback);

	/**
	 * Compile the supplied recipe to a method and load it as supplier. The
	 * resulting supplier instance will be at the top of the stack.
	 */
	public abstract void compileToSupplier(CreationRecipe recipe);

}
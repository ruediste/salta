package com.github.ruediste.salta.core;

import java.util.function.Supplier;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.standard.util.Accessibility;

public interface RecipeCompilationContext {

	/**
	 * Add a field to the generated method and return it's name. The field will
	 * be initialized to the given value
	 */
	<T> FieldHandle addField(Class<T> fieldType, T value);

	/**
	 * Add a field to the compiled recipe class and load it.
	 * 
	 * @param fieldType
	 *            type of the field to be added and loaded
	 * @param value
	 *            value the field is initialized to
	 */
	<T> FieldHandle addFieldAndLoad(Class<T> fieldType, T value);

	void loadField(FieldHandle handle);

	/**
	 * Queue the compilation of a recipe
	 */
	void queueAction(Runnable run);

	/**
	 * Compile the supplied recipe to a method and load it as {@link Supplier}.
	 * The resulting {@link Supplier} instance will be at the top of the stack.
	 */
	void compileToSupplier(SupplierRecipe recipe);

	/**
	 * Return the {@link MethodVisitor} to compile the code to
	 * 
	 * @return
	 */
	GeneratorAdapter getMv();

	RecipeCompiler getCompiler();

	/**
	 * Supports the following casts
	 * <ul>
	 * <li>from a primitive type to the boxed version</li>
	 * <li>from any type to a primitive</li>
	 * <li>downcast from any type to any other</li>
	 * <li>from object to array</li>
	 * <li>array to Object (trivial)</li>
	 * </ul>
	 * 
	 * This method will generate the necessary instructions such that the TOS
	 * value has the requested type. The type which is beeing cast to will
	 * always be public ({@link Accessibility#isClassPublic(Class)})
	 * 
	 * @return the type to which the TOS was casted
	 */
	Class<?> castToPublic(Class<?> from, Class<?> to);

	/**
	 * Pop a value of the given type from the stack. if {@code void.class} is
	 * passed, nothing is popped
	 */
	void pop(Class<?> type);

}
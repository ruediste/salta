package com.github.ruediste.salta.core;

import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CreationRecipeCompilerTest {

	private CreationRecipeCompiler compiler;

	@Before
	public void setup() {
		compiler = new CreationRecipeCompiler();
	}

	@Test
	public void testSimple() {
		CreationRecipe recipe = new CreationRecipe() {

			@Override
			public void compile(MethodVisitor mv,
					RecipeCompilationContext compilationContext) {
				mv.visitInsn(Opcodes.ICONST_3);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
						"valueOf", "(I)Ljava/lang/Integer;", false);
			}
		};

		assertEquals(3, compiler.compile(recipe).get());
	}
}

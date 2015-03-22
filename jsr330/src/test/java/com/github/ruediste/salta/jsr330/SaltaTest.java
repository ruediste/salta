package com.github.ruediste.salta.jsr330;

import static me.qmx.jitescript.util.CodegenUtils.c;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JDKVersion;
import me.qmx.jitescript.JiteClass;

import org.junit.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import com.github.ruediste.salta.jsr330.Salta;

public class SaltaTest {

	public static class DynamicClassLoader extends ClassLoader {
		public Class<?> define(JiteClass jiteClass) {
			byte[] classBytes = jiteClass.toBytes(JDKVersion.V1_7);
			return super.defineClass(c(jiteClass.getClassName()), classBytes,
					0, classBytes.length);
		}
	}

	@Test
	public void emptyInjector() {
		Injector injector = Salta.createInjector();
		assertNotNull(injector);
	}

	@Test
	public void privateMemberSetting() throws Exception {
		final String className = "helloTest";
		JiteClass jiteClass = new JiteClass(className) {
			{

				defineField("target", ACC_PUBLIC, Type.getType(TestClass.class)
						.getDescriptor(), null);

				// create constructor
				defineDefaultConstructor();

				// you can use the pre-constructor style
				defineMethod("doSet", ACC_PUBLIC, sig(void.class),
						new CodeBlock() {
							{
								aload(0);
								getfield(className, "target",
										Type.getType(TestClass.class)
												.getDescriptor());
								ldc(5);

								invokedynamic(
										"foobar",
										sig(void.class, TestClass.class,
												int.class),
										new Handle(
												H_INVOKESTATIC,
												Type.getType(SaltaTest.class)
														.getInternalName(),
												"bootstrap",
												sig(CallSite.class,
														MethodHandles.Lookup.class,
														String.class,
														MethodType.class,
														Class.class,
														String.class)), Type
												.getType(TestClass.class),
										"field");
								voidreturn();
							}
						});

			}
		};

		Class<?> clazz = new DynamicClassLoader().define(jiteClass);
		TestClass target = new TestClass();
		assertEquals(0, target.getField());

		Object accessor = clazz.newInstance();
		clazz.getField("target").set(accessor, target);

		clazz.getMethod("doSet").invoke(accessor);

		assertEquals(5, target.getField());
	}

	public static CallSite bootstrap(MethodHandles.Lookup dummy, String name,
			MethodType type, Class<?> clazz, String fieldName) throws Exception {
		Field f = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
		f.setAccessible(true);
		Lookup lookup = (Lookup) f.get(null);
		MethodHandle setter = lookup.findSetter(clazz, fieldName, int.class);
		return new ConstantCallSite(setter);
	}
}

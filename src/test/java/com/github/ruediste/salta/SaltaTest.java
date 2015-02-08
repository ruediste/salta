package com.github.ruediste.salta;

import static me.qmx.jitescript.util.CodegenUtils.c;
import static me.qmx.jitescript.util.CodegenUtils.ci;
import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import static org.junit.Assert.assertNotNull;

import java.io.PrintStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JiteClass;

import org.junit.Assert;
import org.junit.Test;

import com.github.ruediste.salta.standard.Injector;

public class SaltaTest {

	public static class DynamicClassLoader extends ClassLoader {
		public Class<?> define(JiteClass jiteClass) {
			byte[] classBytes = jiteClass.toBytes();
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
				// you can use the pre-constructor style
				defineMethod("main", ACC_PUBLIC | ACC_STATIC,
						sig(void.class, String[].class), new CodeBlock() {
							{
								ldc("helloWorld");
								getstatic(p(System.class), "out",
										ci(PrintStream.class));
								swap();
								invokevirtual(p(PrintStream.class), "println",
										sig(void.class, Object.class));

								voidreturn();
								
								invokedynamic()
							}
						});
				// or use chained api
				defineMethod("hello", ACC_PUBLIC | ACC_STATIC,
						sig(String.class),
						CodeBlock.newCodeBlock().ldc("helloWorld").areturn());

			}
		};

		Class<?> clazz = new DynamicClassLoader().define(jiteClass);
		Method helloMethod = clazz.getMethod("hello");
		Object result = helloMethod.invoke(null);
		Assert.assertEquals("helloWorld", result);

		Method mainMethod = clazz.getMethod("main", String[].class);
		mainMethod.invoke(null, (Object) new String[] {});
	}

	public static CallSite bootstrap(MethodHandles.Lookup lookup, String name,
			MethodType type) throws Exception {
		MethodHandle setter = MethodHandles.lookup().findSetter(
				TestClass.class, "field", int.class);
		return new ConstantCallSite(setter);
	}
}

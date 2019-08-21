open module com.github.ruediste.salta.test {
	requires com.github.ruediste.salta.core;
	requires com.github.ruediste.salta.jsr330;
	requires org.objectweb.asm;
	requires org.objectweb.asm.commons;
	requires junit;
	requires java.logging;
	requires org.mockito;
	requires guava;
	requires javax.inject.tck;
	requires net.bytebuddy;
	requires net.bytebuddy.agent;

	exports com.github.ruediste.salta.standard.test.recipe;
}
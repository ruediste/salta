module com.github.ruediste.salta.core {
	requires org.objectweb.asm;
	requires org.objectweb.asm.commons;
	requires org.objectweb.asm.util;
	requires transitive guava;
	requires net.bytebuddy;
	requires net.bytebuddy.agent;
	requires transitive jdk.attach;

	exports com.github.ruediste.salta.core;
	exports com.github.ruediste.salta.core.compile;
	exports com.github.ruediste.salta.standard;
	exports com.github.ruediste.salta.matchers;
	exports com.github.ruediste.salta.standard.util;
	exports com.github.ruediste.salta.standard.binder;
	exports com.github.ruediste.salta.standard.recipe;
	exports com.github.ruediste.salta.standard.config;
	exports com.github.ruediste.salta.core.attachedProperties;
}
module com.github.ruediste.salta.core {
	requires org.objectweb.asm;
	requires org.objectweb.asm.commons;
	requires org.objectweb.asm.util;
	requires net.bytebuddy;
	requires net.bytebuddy.agent;
	requires transitive jdk.attach;
	requires com.google.common;

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
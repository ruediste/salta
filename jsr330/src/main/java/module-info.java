module com.github.ruediste.salta.jsr330 {
	requires transitive javax.inject;
	requires transitive com.github.ruediste.salta.core;
	requires java.logging;
	requires net.bytebuddy;
	requires com.google.common;

	exports com.github.ruediste.salta.jsr330;
	exports com.github.ruediste.salta.jsr330.binder;
	exports com.github.ruediste.salta.jsr330.util;

}
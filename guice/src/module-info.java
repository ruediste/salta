module com.github.ruediste.salta.guice {

	exports com.google.inject;
	exports com.google.inject.binder;
	exports com.google.inject.matcher;
	exports com.google.inject.name;
	exports com.google.inject.spi;
	exports com.google.inject.util;
	exports com.github.ruediste.salta.guice;
	exports com.github.ruediste.salta.guice.binder;
	requires com.google.common;
	requires com.github.ruediste.salta.core;
	requires java.logging;
	requires javax.inject;
	requires java.naming;
	requires java.sql;

}
package com.github.ruediste.salta.jsr330;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.inject.Named;

import com.github.ruediste.salta.standard.binder.Binder;

/**
 * Utility methods for use with {@code @}{@link Named}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class Names {

	private Names() {
	}

	/**
	 * Creates a {@link Named} annotation with {@code name} as the value.
	 */
	public static Named named(String name) {
		return new NamedImpl(name);
	}

	/**
	 * Creates a constant binding to {@code @Named(key)} for each entry in
	 * {@code properties}.
	 */
	public static void bindProperties(Binder binder,
			Map<String, String> properties) {
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			binder.bind(String.class).annotatedWith(new NamedImpl(key))
					.toInstance(value);
		}
	}

	/**
	 * Creates a constant binding to {@code @Named(key)} for each property. This
	 * method binds all properties including those inherited from
	 * {@link Properties#defaults defaults}.
	 */
	public static void bindProperties(Binder binder, Properties properties) {

		// use enumeration to include the default properties
		for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
			String propertyName = (String) e.nextElement();
			String value = properties.getProperty(propertyName);
			binder.bind(String.class)
					.annotatedWith(new NamedImpl(propertyName))
					.toInstance(value);
		}
	}
}

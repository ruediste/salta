package com.github.ruediste.simpledi;

import java.util.List;

/**
 * Modules define {@link Rule}s and are used to create {@link Injector}s.
 * 
 * @see SimpleDi
 */
public interface Module {
	List<Rule> getRules();
}

package com.github.ruediste.simpledi;

import java.util.ArrayList;
import java.util.List;

public class Modules {

	public static List<Rule> getRules(List<Module> modules) {
		ArrayList<Rule> result = new ArrayList<>();
		for (Module module : modules) {
			result.addAll(module.getRules());
		}
		return result;
	}
}

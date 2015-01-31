package com.github.ruediste.simpledi;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModule implements Module {

	private List<Rule> rules;

	protected List<Rule> rules() {
		return rules;
	}

	@Override
	public List<Rule> getRules() {
		rules = new ArrayList<>();
		configure();
		List<Rule> tmpRules = rules;
		rules = null;
		return tmpRules;
	}

	protected abstract void configure();

	protected void addRule(Rule rule) {
		rules.add(rule);
	}

	protected AnnotatedConstantBinder bindConstant() {
		return new AnnotatedConstantBinder(rules());
	}
}

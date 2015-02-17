package com.github.ruediste.salta.guice;

import com.github.ruediste.salta.standard.Module;
import com.github.ruediste.salta.standard.binder.Binder;

public class ModuleAdapter implements Module {
	private com.google.inject.Module delegate;

	public ModuleAdapter(com.google.inject.Module delegate) {
		this.delegate = delegate;
	}

	@Override
	public void configure(Binder binder) {
		delegate.configure(null);
	}

}

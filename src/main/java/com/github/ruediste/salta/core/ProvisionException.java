package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.stream.Stream;

public class ProvisionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ProvisionException() {
	}

	public ProvisionException(String message) {
		super("\n" + message);
	}

	public ProvisionException(Throwable cause) {
		super(cause);
	}

	public ProvisionException(String message, Throwable cause) {
		super("\n" + message, cause);
	}

	@Override
	public String getMessage() {
		if (getCause() instanceof ProvisionException)
			return super.getMessage() + getCause().getMessage();
		else
			return super.getMessage();
	}

	public Stream<Throwable> getRecursiveCauses() {
		ArrayList<Throwable> result = new ArrayList<>();
		Throwable t = getCause();
		while (t != null) {
			result.add(t);
			t = t.getCause();
		}
		return result.stream();
	}
}

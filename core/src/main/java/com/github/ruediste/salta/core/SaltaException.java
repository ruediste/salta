package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.stream.Stream;

public class SaltaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SaltaException() {
	}

	public SaltaException(String message) {
		super("\n" + message);
	}

	public SaltaException(Throwable cause) {
		super(cause);
	}

	public SaltaException(String message, Throwable cause) {
		super("\n" + message, cause);
	}

	@Override
	public String getMessage() {
		if (getCause() instanceof SaltaException)
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

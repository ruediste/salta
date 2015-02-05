package com.github.ruediste.simpledi.core;

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

}

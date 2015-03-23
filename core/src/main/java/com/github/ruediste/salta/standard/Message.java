package com.github.ruediste.salta.standard;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.ruediste.salta.standard.binder.SaltaBinder;
import com.google.common.base.Objects;

/**
 * An error message and the context in which it occured. Messages are usually
 * created internally by Salta and its extensions. Messages can be created
 * explicitly in a module using {@link SaltaBinder#addError(Message) addError()}
 * statements:
 * 
 * <pre>
 * try {
 * 	bindPropertiesFromFile();
 * } catch (IOException e) {
 * 	addError(e);
 * }
 * </pre>
 *
 * @author crazybob@google.com (Bob Lee)
 */
public final class Message {
	private final String message;
	private final Throwable cause;

	/**
	 * @since 2.0
	 */
	public Message(String message, Throwable cause) {
		this.message = checkNotNull(message, "message");
		this.cause = cause;
	}

	public Message(String message) {
		this(message, null);
	}

	/**
	 * Gets the error message text.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the throwable that caused this message, or {@code null} if this
	 * message was not caused by a throwable.
	 *
	 * @since 2.0
	 */
	public Throwable getCause() {
		return cause;
	}

	@Override
	public String toString() {
		return message;
	}

	@Override
	public int hashCode() {
		return message.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Message)) {
			return false;
		}
		Message e = (Message) o;
		return message.equals(e.message) && Objects.equal(cause, e.cause);
	}

}

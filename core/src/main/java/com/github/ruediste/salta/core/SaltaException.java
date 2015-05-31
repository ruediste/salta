package com.github.ruediste.salta.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.stream.Stream;

import com.google.common.base.Strings;

public class SaltaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String message;

    public SaltaException() {
    }

    public SaltaException(String message) {
        super(message);
        this.message = message;
    }

    public SaltaException(Throwable cause) {
        super(cause);
    }

    public SaltaException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(super.getMessage());
        getRecursiveCauses().forEach(t -> {
            if (t instanceof InvocationTargetException) {
                return;
            }
            sb.append("\n");
            String msg = t.getMessage();
            if (Strings.isNullOrEmpty(msg)) {
                sb.append(t.getClass().getName());
            } else if (t instanceof SaltaException) {
                sb.append(((SaltaException) t).message);
            } else {
                sb.append(t.getClass().getName() + ": " + msg);
            }
        });
        return sb.toString();
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

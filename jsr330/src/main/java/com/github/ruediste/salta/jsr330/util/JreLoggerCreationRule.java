package com.github.ruediste.salta.jsr330.util;

import java.util.logging.Logger;

/**
 * Creation rule for injecting {@link Logger}s initialized named by the class
 * they are injected into.
 */
public class JreLoggerCreationRule extends LoggerCreationRule {
    public JreLoggerCreationRule() {
        super(Logger.class, declaringClass -> Logger.getLogger(declaringClass.getName()));
    }

}
package io.leego.pharos.enums;

/**
 * @author Yihleego
 */
public enum SchemeStrategy {
    /** Choose a default service if no services were matched. */
    CONSERVATIVE,
    /** Throw service instance unavailable exception if no services were matched. */
    ABORT,
}

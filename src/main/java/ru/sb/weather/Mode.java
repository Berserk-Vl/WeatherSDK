package ru.sb.weather;

/**
 * Used to set the operating mode of the Weather class.
 *
 * @author <a href="https://github.com/Berserk-Vl/">Berserk-Vl</a>
 */
public enum Mode {
    /**
     * A mode in which data is updated only after a request.
     */
    ON_DEMAND,
    /**
     * A mode in which data is updated automatically after a certain
     * period of time or upon request.
     */
    POLLING
}

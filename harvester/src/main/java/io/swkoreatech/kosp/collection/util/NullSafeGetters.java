package io.swkoreatech.kosp.collection.util;

/**
 * Utility class for null-safe conversions from nullable wrapper types to primitive types.
 * Provides helper methods to safely convert Integer/Long to int/long with zero as default.
 */
public final class NullSafeGetters {

    private NullSafeGetters() {
        throw new AssertionError("Utility class");
    }

    /**
     * Returns the integer value or zero if null.
     *
     * @param value the nullable integer
     * @return the value or 0 if null
     */
    public static int intOrZero(Integer value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Returns the long value or zero if null.
     *
     * @param value the nullable long
     * @return the value or 0L if null
     */
    public static long longOrZero(Long value) {
        if (value == null) {
            return 0L;
        }
        return value;
    }
}

/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.util
 */
package io.github.dinamo541.corefx.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Singleton suite of general-purpose validation tools used to guarantee data
 * integrity across CoreFx applications.
 *
 * <p>
 * The class exposes two complementary families of methods:
 * </p>
 * <ul>
 * <li><b>Predicate validators</b> (e.g. {@link #isBlank(String)},
 * {@link #isEmail(String)}, {@link #isInRange(double, double, double)}). These
 * are <em>fully null-safe</em>: they never throw, returning {@code false} when
 * the supplied value cannot satisfy the rule. They are designed to be composed
 * freely in UI bindings, guard clauses, and business logic.</li>
 * <li><b>Contract validators</b> (the {@code require*} methods, e.g.
 * {@link #requireNotBlank(String, String)}). These enforce a precondition and
 * throw a descriptive exception when it is violated, mirroring the defensive
 * style used by {@code FlowController}.</li>
 * </ul>
 *
 * <p>
 * Implements the Singleton pattern using the initialization-on-demand holder
 * idiom, providing lazy, thread-safe access without synchronization overhead.
 * All validation methods are stateless and rely only on the native Java
 * platform, so the library imposes no external dependencies on its consumers.
 * </p>
 *
 * <p>
 * Every regular expression is pre-compiled into an immutable, thread-safe
 * {@link Pattern} constant and written in linear (non-backtracking) form to
 * avoid catastrophic regex performance against hostile input.
 * </p>
 *
 * @author Sem
 * @author Dominique
 * @version 1.0
 * @since 2026/06/10
 */
public final class Validator {

    /**
     * Holder for lazy, thread-safe singleton initialization.
     * The JVM class-loading mechanism guarantees {@code INSTANCE} is created
     * exactly once, only when {@link Validator#getInstance()} is first called.
     */
    private static final class ValidatorHolder {
        private static final Validator INSTANCE = new Validator();
    }

    /** Matches a non-negative integer composed solely of ASCII digits. */
    private static final Pattern NUMERIC = Pattern.compile("\\d+");

    /** Matches an optionally signed integer. */
    private static final Pattern INTEGER = Pattern.compile("[+-]?\\d+");

    /**
     * Matches an optionally signed decimal number (e.g. {@code -12}, {@code 3.14}).
     */
    private static final Pattern DECIMAL = Pattern.compile("[+-]?\\d+(\\.\\d+)?");

    /** Matches one or more Unicode letters. */
    private static final Pattern ALPHABETIC = Pattern.compile("\\p{L}+");

    /**
     * Matches one or more Unicode letters and spaces (no leading/trailing space).
     */
    private static final Pattern ALPHABETIC_SPACES = Pattern.compile("\\p{L}+(\\s\\p{L}+)*");

    /** Matches one or more Unicode letters or digits. */
    private static final Pattern ALPHANUMERIC = Pattern.compile("[\\p{L}\\p{N}]+");

    /**
     * Matches a conservative, RFC-pragmatic e-mail address. Written without
     * nested quantifiers so it evaluates in linear time on any input.
     */
    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}");

    /**
     * Private constructor — use {@link #getInstance()} to obtain the singleton.
     */
    private Validator() {
    }

    /**
     * Returns the singleton instance of {@code Validator}.
     *
     * @return the single {@code Validator} instance
     */
    public static Validator getInstance() {
        return ValidatorHolder.INSTANCE;
    }

    // ---------------------------------------------------------------------
    // Null / presence predicates
    // ---------------------------------------------------------------------

    /**
     * Tests whether the given reference is {@code null}.
     *
     * @param value the reference to test (may be {@code null})
     * @return {@code true} if {@code value} is {@code null}
     */
    public boolean isNull(Object value) {
        return value == null;
    }

    /**
     * Tests whether the given reference is not {@code null}.
     *
     * @param value the reference to test (may be {@code null})
     * @return {@code true} if {@code value} is not {@code null}
     */
    public boolean isNotNull(Object value) {
        return value != null;
    }

    // ---------------------------------------------------------------------
    // String predicates
    // ---------------------------------------------------------------------

    /**
     * Tests whether the given string is {@code null} or has no characters.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} is {@code null} or empty
     */
    public boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Tests whether the given string contains at least one character.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} is non-{@code null} and not empty
     */
    public boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    /**
     * Tests whether the given string is {@code null}, empty, or whitespace only.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} is {@code null} or blank
     */
    public boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Tests whether the given string contains at least one non-whitespace
     * character.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} is non-{@code null} and not blank
     */
    public boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * Tests whether the trimmed length of the string is at least {@code min}.
     *
     * @param value the string to test (may be {@code null})
     * @param min   the minimum allowed length (inclusive)
     * @return {@code true} if {@code value} is non-{@code null} and its trimmed
     *         length is greater than or equal to {@code min}
     */
    public boolean hasMinLength(String value, int min) {
        return value != null && value.trim().length() >= min;
    }

    /**
     * Tests whether the trimmed length of the string is at most {@code max}.
     *
     * @param value the string to test (may be {@code null})
     * @param max   the maximum allowed length (inclusive)
     * @return {@code true} if {@code value} is non-{@code null} and its trimmed
     *         length is less than or equal to {@code max}
     */
    public boolean hasMaxLength(String value, int max) {
        return value != null && value.trim().length() <= max;
    }

    /**
     * Tests whether the trimmed length of the string falls within the inclusive
     * range {@code [min, max]}.
     *
     * @param value the string to test (may be {@code null})
     * @param min   the minimum allowed length (inclusive)
     * @param max   the maximum allowed length (inclusive)
     * @return {@code true} if {@code value} is non-{@code null} and its trimmed
     *         length lies within the range
     */
    public boolean hasLengthBetween(String value, int min, int max) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= min && length <= max;
    }

    /**
     * Tests whether the entire (trimmed) string is composed only of ASCII digits
     * and therefore represents a non-negative integer.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} contains only digits
     */
    public boolean isNumeric(String value) {
        return value != null && NUMERIC.matcher(value.trim()).matches();
    }

    /**
     * Tests whether the entire (trimmed) string represents an optionally signed
     * integer.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} is a valid integer literal
     */
    public boolean isInteger(String value) {
        return value != null && INTEGER.matcher(value.trim()).matches();
    }

    /**
     * Tests whether the entire (trimmed) string represents an optionally signed
     * decimal number.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} is a valid decimal literal
     */
    public boolean isDecimal(String value) {
        return value != null && DECIMAL.matcher(value.trim()).matches();
    }

    /**
     * Tests whether the entire string is composed only of Unicode letters.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} contains only letters
     */
    public boolean isAlphabetic(String value) {
        return value != null && ALPHABETIC.matcher(value).matches();
    }

    /**
     * Tests whether the string is composed only of Unicode letters and single
     * spaces between words (no leading, trailing, or consecutive spaces).
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} is a valid space-separated word
     *         sequence
     */
    public boolean isAlphabeticWithSpaces(String value) {
        return value != null && ALPHABETIC_SPACES.matcher(value).matches();
    }

    /**
     * Tests whether the string is composed only of Unicode letters and digits.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} contains only letters and digits
     */
    public boolean isAlphanumeric(String value) {
        return value != null && ALPHANUMERIC.matcher(value).matches();
    }

    /**
     * Tests whether the (trimmed) string is a syntactically valid e-mail address.
     *
     * @param value the string to test (may be {@code null})
     * @return {@code true} if {@code value} looks like a valid e-mail address
     */
    public boolean isEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
    }

    /**
     * Tests whether the entire string matches the supplied regular expression.
     * Returns {@code false} (rather than throwing) when either argument is
     * {@code null} or when the expression is syntactically invalid.
     *
     * @param value the string to test (may be {@code null})
     * @param regex the regular expression (may be {@code null})
     * @return {@code true} if {@code value} fully matches {@code regex}
     */
    public boolean matches(String value, String regex) {

        if (value == null || regex == null) {
            return false;
        }
        try {
            return Pattern.matches(regex, value);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     * Tests whether the entire string matches the supplied pre-compiled pattern.
     *
     * @param value   the string to test (may be {@code null})
     * @param pattern the pattern to match against (may be {@code null})
     * @return {@code true} if {@code value} fully matches {@code pattern}
     */
    public boolean matches(String value, Pattern pattern) {
        return value != null && pattern != null && pattern.matcher(value).matches();
    }

    // ---------------------------------------------------------------------
    // Numeric range predicates
    // ---------------------------------------------------------------------

    /**
     * Tests whether a {@code long} value lies within the inclusive range
     * {@code [min, max]}. The bounds are normalized, so the order in which they
     * are supplied does not matter.
     *
     * @param value the value to test
     * @param min   one bound of the range
     * @param max   the other bound of the range
     * @return {@code true} if {@code value} lies within the range
     */
    public boolean isInRange(long value, long min, long max) {
        long lower = Math.min(min, max);
        long upper = Math.max(min, max);
        return value >= lower && value <= upper;
    }

    /**
     * Tests whether a {@code double} value lies within the inclusive range
     * {@code [min, max]}. The bounds are normalized, so the order in which they
     * are supplied does not matter. {@code NaN} values never lie within a range.
     *
     * @param value the value to test
     * @param min   one bound of the range
     * @param max   the other bound of the range
     * @return {@code true} if {@code value} lies within the range
     */
    public boolean isInRange(double value, double min, double max) {
        if (Double.isNaN(value) || Double.isNaN(min) || Double.isNaN(max)) {
            return false;
        }
        double lower = Math.min(min, max);
        double upper = Math.max(min, max);
        return value >= lower && value <= upper;
    }

    /**
     * Tests whether the given number is strictly greater than zero.
     *
     * @param value the number to test (may be {@code null})
     * @return {@code true} if {@code value} is non-{@code null} and positive
     */
    public boolean isPositive(Number value) {
        return value != null && value.doubleValue() > 0.0;
    }

    /**
     * Tests whether the given number is strictly less than zero.
     *
     * @param value the number to test (may be {@code null})
     * @return {@code true} if {@code value} is non-{@code null} and negative
     */
    public boolean isNegative(Number value) {
        return value != null && value.doubleValue() < 0.0;
    }

    /**
     * Tests whether the given number is greater than or equal to zero.
     *
     * @param value the number to test (may be {@code null})
     * @return {@code true} if {@code value} is non-{@code null} and not negative
     */
    public boolean isNonNegative(Number value) {
        return value != null && value.doubleValue() >= 0.0;
    }

    // ---------------------------------------------------------------------
    // Collection / map predicates
    // ---------------------------------------------------------------------

    /**
     * Tests whether the given collection is {@code null} or contains no elements.
     *
     * @param collection the collection to test (may be {@code null})
     * @return {@code true} if {@code collection} is {@code null} or empty
     */
    public boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Tests whether the given collection contains at least one element.
     *
     * @param collection the collection to test (may be {@code null})
     * @return {@code true} if {@code collection} is non-{@code null} and not empty
     */
    public boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Tests whether the given map is {@code null} or contains no entries.
     *
     * @param map the map to test (may be {@code null})
     * @return {@code true} if {@code map} is {@code null} or empty
     */
    public boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Tests whether the given map contains at least one entry.
     *
     * @param map the map to test (may be {@code null})
     * @return {@code true} if {@code map} is non-{@code null} and not empty
     */
    public boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Tests whether the given array is {@code null} or has length zero.
     *
     * @param array the array to test (may be {@code null})
     * @return {@code true} if {@code array} is {@code null} or empty
     */
    public boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Tests whether the given array contains at least one element.
     *
     * @param array the array to test (may be {@code null})
     * @return {@code true} if {@code array} is non-{@code null} and not empty
     */
    public boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    // ---------------------------------------------------------------------
    // Contract validators (throwing)
    // ---------------------------------------------------------------------

    /**
     * Ensures the supplied reference is not {@code null}, returning it unchanged.
     *
     * @param <T>     the type of the reference
     * @param value   the reference to validate
     * @param message the detail message used if validation fails
     * @return {@code value}, guaranteed non-{@code null}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public <T> T requireNonNull(T value, String message) {
        return Objects.requireNonNull(value, message);
    }

    /**
     * Ensures the supplied string is not {@code null} or blank, returning the
     * original (untrimmed) value unchanged.
     *
     * @param value the string to validate
     * @param name  the field name used to build the exception message
     * @return {@code value}, guaranteed non-blank
     * @throws IllegalArgumentException if {@code value} is {@code null} or blank
     */
    public String requireNotBlank(String value, String name) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(
                    (name == null ? "value" : name) + " cannot be null or blank");
        }
        return value;
    }

    /**
     * Ensures a {@code long} value lies within the inclusive range
     * {@code [min, max]}, returning it unchanged.
     *
     * @param value the value to validate
     * @param min   one bound of the range
     * @param max   the other bound of the range
     * @param name  the field name used to build the exception message
     * @return {@code value}, guaranteed to be in range
     * @throws IllegalArgumentException if {@code value} is outside the range
     */
    public long requireInRange(long value, long min, long max, String name) {
        if (!isInRange(value, min, max)) {
            throw new IllegalArgumentException(
                    (name == null ? "value" : name) + " (" + value
                            + ") must be between " + Math.min(min, max)
                            + " and " + Math.max(min, max));
        }
        return value;
    }

    // ---------------------------------------------------------------------
    // Object overrides (singleton)
    // ---------------------------------------------------------------------

    /**
     * Returns a string representation of this {@code Validator} singleton.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "Validator{}";
    }

    /**
     * Computes the hash code for this singleton class.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash();
    }

    /**
     * Compares this {@code Validator} singleton with another object for equality.
     *
     * @param obj the object to compare with
     * @return {@code true} if the objects are of the same class
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return true;
    }

    /**
     * Prevents cloning of this singleton class.
     *
     * @return never returns normally
     * @throws CloneNotSupportedException always, to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of Validator is not supported");
    }

}

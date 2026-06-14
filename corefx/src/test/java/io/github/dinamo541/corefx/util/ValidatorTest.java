/*
 * CoreFx - JavaFX utility library
 * Tests for Validator
 * Package: io.github.dinamo541.corefx.util
 */
package io.github.dinamo541.corefx.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Validator}. Covers the null-safe predicate family and
 * the throwing contract validators.
 */
class ValidatorTest {

    private final Validator validator = Validator.getInstance();

    // ---------------------------------------------------------------------
    // Singleton
    // ---------------------------------------------------------------------

    @Test
    void getInstanceReturnsSameInstance() {
        assertSame(Validator.getInstance(), Validator.getInstance());
    }

    // ---------------------------------------------------------------------
    // Null / presence
    // ---------------------------------------------------------------------

    @Test
    void nullPredicates() {
        assertTrue(validator.isNull(null));
        assertFalse(validator.isNull("x"));
        assertTrue(validator.isNotNull("x"));
        assertFalse(validator.isNotNull(null));
    }

    // ---------------------------------------------------------------------
    // String predicates
    // ---------------------------------------------------------------------

    @Test
    void emptyAndBlank() {
        assertTrue(validator.isEmpty((String) null));
        assertTrue(validator.isEmpty(""));
        assertFalse(validator.isEmpty(" "));
        assertTrue(validator.isNotEmpty(" "));

        assertTrue(validator.isBlank(null));
        assertTrue(validator.isBlank("   "));
        assertFalse(validator.isBlank("x"));
        assertTrue(validator.isNotBlank("x"));
    }

    @Test
    void lengthPredicates() {
        assertTrue(validator.hasMinLength("abc", 3));
        assertFalse(validator.hasMinLength("ab", 3));
        assertFalse(validator.hasMinLength(null, 0));

        assertTrue(validator.hasMaxLength("abc", 3));
        assertFalse(validator.hasMaxLength("abcd", 3));

        assertTrue(validator.hasLengthBetween("abc", 2, 4));
        assertFalse(validator.hasLengthBetween("a", 2, 4));
        assertFalse(validator.hasLengthBetween(null, 0, 10));
    }

    @Test
    void numericFamily() {
        assertTrue(validator.isNumeric("123"));
        assertFalse(validator.isNumeric("-1"));
        assertFalse(validator.isNumeric("1.2"));
        assertFalse(validator.isNumeric(null));

        assertTrue(validator.isInteger("-42"));
        assertTrue(validator.isInteger("+42"));
        assertFalse(validator.isInteger("4.2"));

        assertTrue(validator.isDecimal("-3.14"));
        assertTrue(validator.isDecimal("10"));
        assertFalse(validator.isDecimal("abc"));
    }

    @Test
    void alphabeticFamilySupportsUnicode() {
        assertTrue(validator.isAlphabetic("María"));
        assertTrue(validator.isAlphabetic("Ñoño"));
        assertFalse(validator.isAlphabetic("Jose1"));
        assertFalse(validator.isAlphabetic("Jose Maria"));

        assertTrue(validator.isAlphabeticWithSpaces("Jose Maria"));
        assertTrue(validator.isAlphabeticWithSpaces("María José"));
        assertFalse(validator.isAlphabeticWithSpaces(" leading"));
        assertFalse(validator.isAlphabeticWithSpaces("double  space"));

        assertTrue(validator.isAlphanumeric("abc123"));
        assertFalse(validator.isAlphanumeric("abc 123"));
    }

    @Test
    void emailValidation() {
        assertTrue(validator.isEmail("user@example.com"));
        assertTrue(validator.isEmail("first.last+tag@sub.domain.co"));
        assertFalse(validator.isEmail("no-at-symbol"));
        assertFalse(validator.isEmail("@nope.com"));
        assertFalse(validator.isEmail("trailing@dot."));
        assertFalse(validator.isEmail(null));
    }

    @Test
    void matchesIsNullSafeAndRejectsBadRegex() {
        assertTrue(validator.matches("abc", "a.c"));
        assertFalse(validator.matches(null, "a.c"));
        assertFalse(validator.matches("abc", (String) null));
        // Unbalanced bracket — invalid regex must yield false, not throw.
        assertFalse(validator.matches("abc", "["));

        Pattern p = Pattern.compile("\\d+");
        assertTrue(validator.matches("123", p));
        assertFalse(validator.matches("12a", p));
        assertFalse(validator.matches("123", (Pattern) null));
    }

    // ---------------------------------------------------------------------
    // Numeric ranges
    // ---------------------------------------------------------------------

    @Test
    void isInRangeNormalizesBounds() {
        assertTrue(validator.isInRange(5L, 0L, 10L));
        assertTrue(validator.isInRange(5L, 10L, 0L));
        assertFalse(validator.isInRange(11L, 0L, 10L));

        assertTrue(validator.isInRange(2.5, 0.0, 5.0));
        assertTrue(validator.isInRange(2.5, 5.0, 0.0));
        assertFalse(validator.isInRange(Double.NaN, 0.0, 5.0));
    }

    @Test
    void signPredicates() {
        assertTrue(validator.isPositive(1));
        assertFalse(validator.isPositive(0));
        assertFalse(validator.isPositive(null));

        assertTrue(validator.isNegative(-1));
        assertFalse(validator.isNegative(0));

        assertTrue(validator.isNonNegative(0));
        assertFalse(validator.isNonNegative(-1));
    }

    // ---------------------------------------------------------------------
    // Collections / maps / arrays
    // ---------------------------------------------------------------------

    @Test
    void containerEmptiness() {
        assertTrue(validator.isEmpty((java.util.Collection<?>) null));
        assertTrue(validator.isEmpty(List.of()));
        assertTrue(validator.isNotEmpty(List.of(1)));

        assertTrue(validator.isEmpty((Map<?, ?>) null));
        assertTrue(validator.isEmpty(Map.of()));
        assertTrue(validator.isNotEmpty(Map.of("k", "v")));

        assertTrue(validator.isEmpty((Object[]) null));
        assertTrue(validator.isEmpty(new Object[0]));
        assertTrue(validator.isNotEmpty(new Object[] { 1 }));
    }

    // ---------------------------------------------------------------------
    // Contract validators (throwing)
    // ---------------------------------------------------------------------

    @Test
    void requireNonNullReturnsValueOrThrows() {
        String value = "x";
        assertSame(value, validator.requireNonNull(value, "must not be null"));
        assertThrows(NullPointerException.class,
                () -> validator.requireNonNull(null, "must not be null"));
    }

    @Test
    void requireNotBlankReturnsValueOrThrows() {
        assertEquals("x", validator.requireNotBlank("x", "name"));
        assertThrows(IllegalArgumentException.class,
                () -> validator.requireNotBlank("  ", "name"));
        assertThrows(IllegalArgumentException.class,
                () -> validator.requireNotBlank(null, "name"));
    }

    @Test
    void requireInRangeLong() {
        assertEquals(5L, validator.requireInRange(5L, 0L, 10L, "n"));
        assertThrows(IllegalArgumentException.class,
                () -> validator.requireInRange(11L, 0L, 10L, "n"));
    }

    @Test
    void requireInRangeDouble() {
        assertEquals(2.5, validator.requireInRange(2.5, 0.0, 5.0, "n"));
        assertThrows(IllegalArgumentException.class,
                () -> validator.requireInRange(9.0, 0.0, 5.0, "n"));
        assertThrows(IllegalArgumentException.class,
                () -> validator.requireInRange(Double.NaN, 0.0, 5.0, "n"));
    }
}

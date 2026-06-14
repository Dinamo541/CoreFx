/*
 * CoreFx - JavaFX utility library
 * Tests for Format
 * Package: io.github.dinamo541.corefx.ui
 */
package io.github.dinamo541.corefx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.text.DecimalFormat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the non-UI surface of {@link Format}: the singleton, the
 * date/decimal formatters, and the defensive copy returned by
 * {@link Format#getDecimalFormat()}. The {@code TextFormatter} factories are
 * only checked for non-null construction, since exercising their filters
 * requires a live JavaFX control.
 */
class FormatTest {

    private final Format format = Format.getInstance();

    @Test
    void getInstanceReturnsSameInstance() {
        assertSame(Format.getInstance(), Format.getInstance());
    }

    @Test
    void dateFormattersArePresent() {
        assertNotNull(format.formatDateShort);
        assertNotNull(format.formatDateMedium);
    }

    @Test
    void getDecimalFormatMatchesPattern() {
        // Compare against a fresh formatter built from the same pattern so the
        // assertion is independent of the host's default locale symbols.
        DecimalFormat reference = new DecimalFormat("#,###,###,##0.00");
        assertEquals(reference.format(1234567.89), format.getDecimalFormat().format(1234567.89));
        assertEquals(reference.format(0), format.getDecimalFormat().format(0));
    }

    @Test
    void getDecimalFormatReturnsDefensiveCopies() {
        DecimalFormat first = format.getDecimalFormat();
        DecimalFormat second = format.getDecimalFormat();
        assertNotSame(first, second);

        // Mutating one copy must not leak into the next one handed out.
        first.applyPattern("0");
        assertEquals(new DecimalFormat("#,###,###,##0.00").format(1234.5),
                format.getDecimalFormat().format(1234.5));
    }

    @Test
    void textFormatterFactoriesConstructNonNull() {
        assertNotNull(format.twoDecimalFormat());
        assertNotNull(format.integerFormat());
        assertNotNull(format.idFormat(10));
        assertNotNull(format.lettersFormat(10));
        assertNotNull(format.maxLengthFormat(10));
    }
}

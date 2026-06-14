/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.ui
 */
package io.github.dinamo541.corefx.ui;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.regex.Pattern;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;

/**
 * Singleton utility class for format management and text input validation in
 * JavaFX.
 * Provides pre-configured formatters for dates, decimal numbers, and custom
 * {@link TextFormatter}s for validating user input in text fields.
 *
 * <p>
 * Includes formatters for:
 * </p>
 * <ul>
 * <li>Date formatting (short and medium styles)</li>
 * <li>Decimal number formatting</li>
 * <li>Integer-only input validation</li>
 * <li>ID/Cedula validation</li>
 * <li>Letter-only input validation (Unicode-aware)</li>
 * <li>Maximum length input validation</li>
 * </ul>
 *
 * @author Carranza
 * @author Dominique
 * @version 2.4
 * @since 2026/06/10
 */
public final class Format {

    /**
     * Inner static class responsible for holding the singleton instance of Format.
     * This approach ensures thread-safe lazy initialization without the need for
     * synchronized blocks.
     */
    private static final class FormatHolder {
        private static final Format INSTANCE = new Format();
    }

    /**
     * Validates an integer composed solely of ASCII digits. Pre-compiled once so
     * it is not rebuilt on every keystroke.
     */
    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");

    /**
     * Validates a non-negative decimal with up to two fractional digits (after
     * removing thousands separators). Pre-compiled once for reuse.
     */
    private static final Pattern TWO_DECIMAL_PATTERN = Pattern.compile("^[0-9]*+(\\.[0-9]{0,2})?$");

    /**
     * {@link DateTimeFormatter} for displaying dates in short format (e.g.,
     * "1/15/09"). Immutable and thread-safe, so it is safe to share.
     */
    public final DateTimeFormatter formatDateShort = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

    /**
     * {@link DateTimeFormatter} for displaying dates in medium format (e.g., "Jan
     * 15, 2009"). Immutable and thread-safe, so it is safe to share.
     */
    public final DateTimeFormatter formatDateMedium = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    /**
     * {@link DecimalFormat} for formatting decimal numbers with a thousands
     * separator (e.g., "1,234,567.89"). Kept private because {@code DecimalFormat}
     * is mutable and not thread-safe; callers obtain a private copy through
     * {@link #getDecimalFormat()}.
     */
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###,###,##0.00");

    /**
     * Private constructor to prevent instantiation.
     * Use {@link #getInstance()} to obtain the singleton instance.
     */
    private Format() {
    }

    /**
     * Returns the singleton instance of Format.
     *
     * @return the singleton instance of Format
     */
    public static Format getInstance() {
        return FormatHolder.INSTANCE;
    }

    /**
     * Returns a fresh, independent copy of the decimal formatter. A copy is
     * returned (rather than the shared instance) because {@link DecimalFormat} is
     * mutable and not thread-safe, so callers can configure and use it without
     * affecting other callers.
     *
     * @return a new {@link DecimalFormat} configured with the thousands-separated
     *         pattern
     */
    public DecimalFormat getDecimalFormat() {
        return (DecimalFormat) decimalFormat.clone();
    }

    /**
     * Creates a TextFormatter that validates decimal numbers with up to 2 decimal
     * places.
     * Accepts input containing thousands separators (commas).
     * Valid formats: "123", "123.45", "1,234.56", "1,234,567.89"
     *
     * @return a TextFormatter for validating two-decimal number input
     */
    public TextFormatter<String> twoDecimalFormat() {
        TextFormatter<String> numericFormat = new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }
            if (c.getControlNewText().contains(",")) {
                ParsePosition parsePosition = new ParsePosition(0);
                Object object = decimalFormat.parse(c.getControlNewText(), parsePosition);

                if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
                    return null;
                } else if (TWO_DECIMAL_PATTERN.matcher(c.getControlNewText().replace(",", "")).matches()) {
                    return c;
                } else {
                    return null;
                }
            } else if (TWO_DECIMAL_PATTERN.matcher(c.getControlNewText().replace(",", "")).matches()) {
                return c;
            } else {
                return null;
            }
        });
        return numericFormat;
    }

    /**
     * Creates a TextFormatter that validates integer-only input.
     * Rejects any non-numeric characters, decimal points, and negative signs.
     * Valid formats: "0", "123", "1234567"
     *
     * @return a TextFormatter for validating integer input
     */
    public TextFormatter<String> integerFormat() {
        TextFormatter<String> numericFormat = new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }
            if (INTEGER_PATTERN.matcher(c.getControlNewText()).matches()) {
                return c;
            } else {
                return null;
            }
        });
        return numericFormat;
    }

    /**
     * Creates a TextFormatter that validates national ID input.
     * Accepts alphanumeric characters and hyphens, preventing consecutive hyphens.
     * Enforces a maximum length limit if specified.
     * Valid formats: "12345-6789", "ABC-123-DEF"
     *
     * @param maxLength the maximum allowed length (0 or negative for unlimited)
     * @return a TextFormatter for validating ID input
     */
    public TextFormatter<String> idFormat(int maxLength) {
        TextFormatter<String> idFormatter = new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }
            if (maxLength > 0) {
                if (((TextInputControl) c.getControl()).getLength() >= maxLength && !c.isDeleted()) {
                    return null;
                }
                if (c.getText().length() > maxLength && !c.isDeleted()) {
                    return null;
                }
            }
            c.setText(c.getText().replaceAll("[^a-zA-Z0-9-]", ""));
            if (c.getControlNewText().matches(".*-{2,}.*")) {
                return null;
            }
            return c;

        });
        return idFormatter;
    }

    /**
     * Creates a TextFormatter that validates letter-only input with optional
     * spaces.
     * Accepts Unicode letters (including accents, e.g. "María", "Ñoño"), rejecting
     * digits and special characters. Prevents consecutive spaces.
     * Enforces a maximum length limit if specified.
     * Valid formats: "John", "Jose Maria", "María José"
     *
     * @param maxLength the maximum allowed length (0 or negative for unlimited)
     * @return a TextFormatter for validating letter-only input
     */
    public TextFormatter<String> lettersFormat(int maxLength) {
        TextFormatter<String> lettersFormatter = new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }
            if (maxLength > 0) {
                if (((TextInputControl) c.getControl()).getLength() >= maxLength && !c.isDeleted()) {
                    return null;
                }
                if (c.getText().length() > maxLength && !c.isDeleted()) {
                    return null;
                }
            }
            if (c.getControlNewText().matches(".*[^\\p{L} ].*")) {
                return null;
            }
            if (c.getControlNewText().matches(".*\\s{2,}.*")) {
                return null;
            }
            return c;

        });
        return lettersFormatter;
    }

    /**
     * Creates a TextFormatter that enforces a maximum length limit on text input.
     * Prevents the user from typing beyond the specified length.
     * Allows deletion of characters.
     *
     * @param length the maximum allowed text length
     * @return a TextFormatter for enforcing maximum length
     */
    public TextFormatter<String> maxLengthFormat(int length) {
        TextFormatter<String> maxLengthFormat = new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }

            if (((TextInputControl) c.getControl()).getLength() >= length && !c.isDeleted()) {
                return null;
            }
            if (c.getText().length() > length && !c.isDeleted()) {
                return null;
            }
            return c;
        });
        return maxLengthFormat;
    }

    /**
     * Returns a string representation of this Format singleton.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "Format{}";
    }

    /**
     * Computes the hash code for this singleton class.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash();
    }

    /**
     * Compares this Format singleton with another object for equality.
     *
     * @param obj the object to compare with
     * @return true if the objects are of the same class, false otherwise
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
     * @throws CloneNotSupportedException always, to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of Format is not supported");
    }

}

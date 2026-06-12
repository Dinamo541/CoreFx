/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
 * TextFormatters for validating user input in text fields.
 * 
 * Includes formatters for:
 * - Date formatting (short and medium styles)
 * - Decimal number formatting
 * - Integer-only input validation
 * - ID/Cedula validation
 * - Letter-only input validation
 * - Maximum length input validation
 * 
 * @author Carranza
 * @author Dominique
 * @version 2.3
 * @since 2026/06/10
 */
public class Format {

    /**
     * Inner static class responsible for holding the singleton instance of Format.
     * This approach ensures thread-safe lazy initialization without the need for
     * synchronized blocks.
     */
    private static class FormatHolder {
        private static final Format INSTANCE = new Format();
    }

    /**
     * DateTimeFormatter for displaying dates in short format (e.g., "1/15/09").
     */
    public DateTimeFormatter formatDateShort = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

    /**
     * DateTimeFormatter for displaying dates in medium format (e.g., "Jan 15,
     * 2009").
     */
    public DateTimeFormatter formatDateMedium = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    /**
     * DecimalFormat for formatting decimal numbers with thousands separator (e.g.,
     * "1,234,567.89").
     */
    public DecimalFormat decimalFormat = new DecimalFormat("#,###,###,##0.00");

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
                } else {
                    Pattern validDoubleText = Pattern.compile("^[0-9]*+(\\.[0-9]{0,2})?$");
                    if (validDoubleText.matcher(c.getControlNewText().replace(",", "")).matches()) {
                        return c;
                    } else {
                        return null;
                    }
                }
            } else {
                Pattern validDoubleText = Pattern.compile("^[0-9]*+(\\.[0-9]{0,2})?$");
                if (validDoubleText.matcher(c.getControlNewText().replace(",", "")).matches()) {
                    return c;
                } else {
                    return null;
                }
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

            Pattern validDoubleText = Pattern.compile("\\d+");
            if (validDoubleText.matcher(c.getControlNewText()).matches()) {
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
    public TextFormatter<String> idFormat(Integer maxLength) {
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
     * Rejects numbers and special characters. Prevents consecutive spaces.
     * Enforces a maximum length limit if specified.
     * Valid formats: "John", "Jose Maria", "Maria Jose"
     * 
     * @param maxLength the maximum allowed length (0 or negative for unlimited)
     * @return a TextFormatter for validating letter-only input
     */
    public TextFormatter<String> lettersFormat(Integer maxLength) {
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
            if (c.getControlNewText().matches(".*[^a-zA-Z ].*")) {
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
    public TextFormatter<String> maxLengthFormat(Integer length) {
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
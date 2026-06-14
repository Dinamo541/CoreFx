/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.ui
 */
package io.github.dinamo541.corefx.ui;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Utility class for configuring and managing JavaFX {@link TableView} controls.
 *
 * <p>
 * The helpers favour a type-safe, lambda-based style over reflection where
 * possible: {@link #createColumn(String, Function)} derives a column's value
 * from an extractor function, avoiding the stringly-typed pitfalls of
 * {@link PropertyValueFactory} (which is still offered via
 * {@link #createPropertyColumn(String, String)} for JavaBean models).
 * Additional
 * helpers cover item population, selection, placeholders, and a ready-made
 * search filter that integrates with the table's own sorting.
 * </p>
 *
 * <p>
 * This is a utility class and should not be instantiated.
 * </p>
 *
 * @author Sem
 * @author Dominique
 * @version 1.2
 * @since 2026/06/10
 */
public final class TableUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TableUtils() {
    }

    // ---------------------------------------------------------------------
    // Column creation
    // ---------------------------------------------------------------------

    /**
     * Creates a column whose cell value is derived from each row by the given
     * extractor function. This is type-safe and independent of property naming.
     *
     * @param <S>       the table row type
     * @param <T>       the column value type
     * @param title     the column header text (must not be {@code null})
     * @param extractor the function mapping a row to its cell value (must not be
     *                  {@code null})
     * @return a configured {@link TableColumn}
     * @throws NullPointerException if {@code title} or {@code extractor} is
     *                              {@code null}
     */
    public static <S, T> TableColumn<S, T> createColumn(String title, Function<S, T> extractor) {
        Objects.requireNonNull(title, "title cannot be null");
        Objects.requireNonNull(extractor, "extractor cannot be null");
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(extractor.apply(data.getValue())));
        return column;
    }

    /**
     * Creates a column bound to a JavaBean property by name, using a
     * {@link PropertyValueFactory}. Prefer {@link #createColumn(String, Function)}
     * for compile-time safety.
     *
     * @param <S>          the table row type
     * @param <T>          the column value type
     * @param title        the column header text (must not be {@code null})
     * @param propertyName the JavaBean property name (must not be {@code null} or
     *                     blank)
     * @return a configured {@link TableColumn}
     * @throws NullPointerException     if {@code title} is {@code null}
     * @throws IllegalArgumentException if {@code propertyName} is {@code null} or
     *                                  blank
     */
    public static <S, T> TableColumn<S, T> createPropertyColumn(String title, String propertyName) {
        Objects.requireNonNull(title, "title cannot be null");
        if (propertyName == null || propertyName.isBlank()) {
            throw new IllegalArgumentException("propertyName cannot be null or blank");
        }
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }

    /**
     * Creates a column via {@link #createColumn(String, Function)} and appends it
     * to the table.
     *
     * @param <S>       the table row type
     * @param <T>       the column value type
     * @param table     the target table (must not be {@code null})
     * @param title     the column header text (must not be {@code null})
     * @param extractor the function mapping a row to its cell value (must not be
     *                  {@code null})
     * @return the column that was added
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <S, T> TableColumn<S, T> addColumn(TableView<S> table, String title, Function<S, T> extractor) {
        Objects.requireNonNull(table, "table cannot be null");
        TableColumn<S, T> column = createColumn(title, extractor);
        table.getColumns().add(column);
        return column;
    }

    // ---------------------------------------------------------------------
    // Items
    // ---------------------------------------------------------------------

    /**
     * Replaces the table's items with the contents of the given collection. A
     * {@code null} collection clears the table.
     *
     * @param <S>   the table row type
     * @param table the target table (must not be {@code null})
     * @param items the items to display (may be {@code null})
     * @return the {@link ObservableList} now backing the table
     * @throws NullPointerException if {@code table} is {@code null}
     */
    public static <S> ObservableList<S> setItems(TableView<S> table, Collection<S> items) {
        Objects.requireNonNull(table, "table cannot be null");
        ObservableList<S> observable = items == null
                ? FXCollections.observableArrayList()
                : FXCollections.observableArrayList(items);
        table.setItems(observable);
        return observable;
    }

    /**
     * Sets the placeholder text shown when the table has no visible rows.
     *
     * @param <S>   the table row type
     * @param table the target table (must not be {@code null})
     * @param text  the placeholder text (a {@code null} value shows no
     *              placeholder)
     * @throws NullPointerException if {@code table} is {@code null}
     */
    public static <S> void setPlaceholder(TableView<S> table, String text) {
        Objects.requireNonNull(table, "table cannot be null");
        table.setPlaceholder(text == null ? null : new Label(text));
    }

    // ---------------------------------------------------------------------
    // Selection
    // ---------------------------------------------------------------------

    /**
     * Enables multiple-row selection on the table.
     *
     * @param <S>   the table row type
     * @param table the target table (must not be {@code null})
     * @throws NullPointerException if {@code table} is {@code null}
     */
    public static <S> void enableMultiSelection(TableView<S> table) {
        Objects.requireNonNull(table, "table cannot be null");
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Returns the currently selected item, or {@code null} if none is selected.
     *
     * @param <S>   the table row type
     * @param table the target table (must not be {@code null})
     * @return the selected item, or {@code null}
     * @throws NullPointerException if {@code table} is {@code null}
     */
    public static <S> S getSelectedItem(TableView<S> table) {
        Objects.requireNonNull(table, "table cannot be null");
        return table.getSelectionModel().getSelectedItem();
    }

    /**
     * Returns an unmodifiable snapshot of the currently selected items.
     *
     * @param <S>   the table row type
     * @param table the target table (must not be {@code null})
     * @return an unmodifiable list of selected items (never {@code null})
     * @throws NullPointerException if {@code table} is {@code null}
     */
    public static <S> List<S> getSelectedItems(TableView<S> table) {
        Objects.requireNonNull(table, "table cannot be null");
        return List.copyOf(table.getSelectionModel().getSelectedItems());
    }

    /**
     * Clears the current selection.
     *
     * @param <S>   the table row type
     * @param table the target table (must not be {@code null})
     * @throws NullPointerException if {@code table} is {@code null}
     */
    public static <S> void clearSelection(TableView<S> table) {
        Objects.requireNonNull(table, "table cannot be null");
        table.getSelectionModel().clearSelection();
    }

    /**
     * Selects the first row, if the table has any rows.
     *
     * @param <S>   the table row type
     * @param table the target table (must not be {@code null})
     * @throws NullPointerException if {@code table} is {@code null}
     */
    public static <S> void selectFirst(TableView<S> table) {
        Objects.requireNonNull(table, "table cannot be null");
        table.getSelectionModel().selectFirst();
    }

    // ---------------------------------------------------------------------
    // Filtering
    // ---------------------------------------------------------------------

    /**
     * Installs a live search filter that narrows the table as the user types in
     * the given text field.
     *
     * <p>
     * The source list is wrapped in a {@link FilteredList} (for matching) and a
     * {@link SortedList} (whose comparator is bound to the table), so filtering
     * and column sorting coexist. The {@code matcher} receives each row and the
     * trimmed query text; any exception it throws is treated as "no match" so a
     * faulty predicate can never break the table. An empty query shows all rows.
     * </p>
     *
     * @param <S>         the table row type
     * @param table       the target table (must not be {@code null})
     * @param source      the backing list of all rows (must not be {@code null})
     * @param searchField the text field driving the filter (must not be
     *                    {@code null})
     * @param matcher     predicate testing a row against the query (must not be
     *                    {@code null})
     * @return the {@link FilteredList} backing the table, for further tuning
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <S> FilteredList<S> installFilter(TableView<S> table, ObservableList<S> source,
            TextField searchField, BiPredicate<S, String> matcher) {
        Objects.requireNonNull(table, "table cannot be null");
        Objects.requireNonNull(source, "source cannot be null");
        Objects.requireNonNull(searchField, "searchField cannot be null");
        Objects.requireNonNull(matcher, "matcher cannot be null");

        FilteredList<S> filtered = new FilteredList<>(source, item -> true);
        searchField.textProperty().addListener((observable, oldText, newText) -> {
            String query = newText == null ? "" : newText.trim();
            if (query.isEmpty()) {
                filtered.setPredicate(item -> true);
            } else {
                filtered.setPredicate(item -> {
                    try {
                        return matcher.test(item, query);
                    } catch (RuntimeException ex) {
                        return false;
                    }
                });
            }
        });

        SortedList<S> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        return filtered;
    }

    /**
     * Returns a string representation of this utility class.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "TableUtils{}";
    }

    /**
     * Computes the hash code for this utility class.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash();
    }

    /**
     * Compares this utility class with another object for equality.
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
     * Prevents cloning of this utility class.
     *
     * @return never returns normally
     * @throws CloneNotSupportedException always, to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of TableUtils is not supported");
    }

}

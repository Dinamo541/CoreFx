/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.navigation
 */
package io.github.dinamo541.corefx.navigation;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton store for global application state, exposed as a thread-safe
 * key-value map that any part of the application can read from and write to.
 *
 * <p>
 * Where {@link FlowController} handles view navigation and {@link StageManager}
 * handles windows, {@code AppContext} holds the shared, cross-cutting data those
 * flows need — the current user, a selected record, feature flags, and so on —
 * without coupling unrelated screens to one another. The three classes are
 * complementary and commonly used together.
 * </p>
 *
 * <p>
 * Implements the Singleton pattern using the initialization-on-demand holder
 * idiom, providing lazy, thread-safe access without synchronization overhead.
 * The backing store is a {@link ConcurrentHashMap}, so reads and writes are
 * lock-free on the common path and safe to call from multiple threads.
 * </p>
 *
 * <p>
 * <b>Null-safety and contracts.</b> Keys must never be {@code null} or blank and
 * values must never be {@code null}; both are rejected with descriptive
 * exceptions, mirroring the defensive style of {@code FlowController} and
 * {@code StageManager}. The {@code null}-value restriction also matches
 * {@link ConcurrentHashMap}, which forbids {@code null} keys and values.
 * Lookups ({@link #get(String)}, {@link #getOrDefault(String, Object)},
 * {@link #contains(String)}) never throw for a missing key.
 * </p>
 *
 * @author Carranza
 * @author Dominique
 * @version 3.0
 * @since 2026/06/10
 */
public final class AppContext {

    // =========================================================================
    // SECTION 1: SINGLETON & FIELDS
    // =========================================================================

    /**
     * Holder for lazy, thread-safe singleton initialization.
     * The JVM class-loading mechanism guarantees {@code INSTANCE} is created
     * exactly once, only when {@link #getInstance()} is first called.
     */
    private static final class AppContextHolder {
        private static final AppContext INSTANCE = new AppContext();
    }

    /**
     * Thread-safe store of application state keyed by an application-chosen name.
     * {@link ConcurrentHashMap} allows lock-free reads and atomic updates without
     * an external lock.
     */
    private final ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<>();

    /**
     * Private constructor — use {@link #getInstance()} to obtain the singleton.
     */
    private AppContext() {
    }

    /**
     * Returns the singleton instance of {@code AppContext}.
     *
     * @return the single {@code AppContext} instance
     */
    public static AppContext getInstance() {
        return AppContextHolder.INSTANCE;
    }

    // =========================================================================
    // SECTION 2: WRITE OPERATIONS
    // =========================================================================

    /**
     * Stores a value under the given key, replacing any value previously stored
     * under the same key.
     *
     * @param key   the key under which the value is stored (must not be
     *              {@code null} or blank)
     * @param value the value to store (must not be {@code null})
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     * @throws NullPointerException     if {@code value} is {@code null}
     */
    public void put(String key, Object value) {
        validateKey(key);
        Objects.requireNonNull(value, "value cannot be null");
        context.put(key, value);
    }

    /**
     * Stores a value under the given key only if no value is currently
     * associated with it.
     *
     * @param key   the key under which the value is stored (must not be
     *              {@code null} or blank)
     * @param value the value to store (must not be {@code null})
     * @return the value previously associated with {@code key}, or {@code null}
     *         if there was none and the new value was stored
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     * @throws NullPointerException     if {@code value} is {@code null}
     */
    public Object putIfAbsent(String key, Object value) {
        validateKey(key);
        Objects.requireNonNull(value, "value cannot be null");
        return context.putIfAbsent(key, value);
    }

    /**
     * Removes the value stored under the given key, if any.
     *
     * @param key the key to remove (must not be {@code null} or blank)
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    public void remove(String key) {
        validateKey(key);
        context.remove(key);
    }

    /**
     * Removes all entries from the application context.
     */
    public void clear() {
        context.clear();
    }

    // =========================================================================
    // SECTION 3: READ OPERATIONS
    // =========================================================================

    /**
     * Retrieves the value stored under the given key, cast to the type expected
     * by the caller.
     *
     * <p>
     * The target type {@code T} is inferred from the assignment context, so
     * {@code String name = context.get("user")} casts automatically. When there
     * is no target type, {@code T} infers to {@link Object}. The cast is
     * unchecked: a {@link ClassCastException} is thrown at the use site if the
     * stored value is not assignment-compatible with {@code T}.
     * </p>
     *
     * @param <T> the expected type of the stored value
     * @param key the key of the value to retrieve (must not be {@code null} or
     *            blank)
     * @return the value associated with {@code key}, or {@code null} if none
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        validateKey(key);
        return (T) context.get(key);
    }

    /**
     * Retrieves the value stored under the given key, returning a fallback when
     * no value is present.
     *
     * @param key          the key of the value to retrieve (must not be
     *                     {@code null} or blank)
     * @param defaultValue the value to return when {@code key} is absent (may be
     *                     {@code null})
     * @return the value associated with {@code key}, or {@code defaultValue} if
     *         none
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    public Object getOrDefault(String key, Object defaultValue) {
        validateKey(key);
        return context.getOrDefault(key, defaultValue);
    }

    /**
     * Returns whether a value is stored under the given key.
     *
     * @param key the key to check (must not be {@code null} or blank)
     * @return {@code true} if a value is stored under {@code key}
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    public boolean contains(String key) {
        validateKey(key);
        return context.containsKey(key);
    }

    /**
     * Returns whether the application context holds no entries.
     *
     * @return {@code true} if the context is empty
     */
    public boolean isEmpty() {
        return context.isEmpty();
    }

    /**
     * Returns the number of entries currently stored in the application context.
     *
     * @return the entry count
     */
    public int size() {
        return context.size();
    }

    // =========================================================================
    // SECTION 4: VALIDATION HELPERS
    // =========================================================================

    /**
     * Validates a context key.
     *
     * @param key the key to validate
     * @throws IllegalArgumentException if {@code key} is {@code null} or blank
     */
    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Context key cannot be null or blank");
        }
    }

    // =========================================================================
    // SECTION 5: OBJECT OVERRIDES (singleton)
    // =========================================================================

    /**
     * Returns a string representation of this {@code AppContext}.
     *
     * <p>
     * Only the entry count is exposed; the stored values are intentionally
     * omitted to avoid leaking application state into logs.
     * </p>
     *
     * @return string representation including the entry count
     */
    @Override
    public String toString() {
        return "AppContext{size=" + context.size() + "}";
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
     * Compares this {@code AppContext} singleton with another object for
     * equality.
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
        throw new CloneNotSupportedException("Cloning of AppContext is not supported");
    }

}

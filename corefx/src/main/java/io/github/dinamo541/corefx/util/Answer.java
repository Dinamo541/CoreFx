/*
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.util
 */
package io.github.dinamo541.corefx.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Generic, immutable-friendly response object for application operations.
 *
 * <p>
 * An {@code Answer} bundles together the outcome of an operation:
 * </p>
 * <ul>
 * <li>a {@link #getState() state} flag indicating success or failure,</li>
 * <li>a user-facing {@link #getMessage() message},</li>
 * <li>a technical {@link #getInternalMessage() internal message} for logs and
 * debugging,</li>
 * <li>and an arbitrary keyed {@link #getResults() result} payload.</li>
 * </ul>
 *
 * <p>
 * <b>Resilience guarantees.</b> The internal result map is {@code final} and is
 * never {@code null}; every constructor initializes it. A {@link LinkedHashMap}
 * is used so that result keys preserve their insertion order, giving
 * deterministic {@link #toString()} output. {@link #getResults()} returns an
 * <em>unmodifiable view</em>, so the only supported mutation path is
 * {@link #setResult(String, Object)} — external code cannot corrupt the payload
 * by accident. Result keys may never be {@code null}, but values may.
 * </p>
 *
 * <p>
 * For concise construction prefer the static factory methods
 * ({@link #ok()}, {@link #success(String)}, {@link #failure(String)}) combined
 * with the fluent {@link #with(String, Object)} builder.
 * </p>
 *
 * @author Carranza
 * @author Dominique
 * @author Sem
 * @version 2.1
 * @since 2026/06/10
 */
public class Answer implements java.io.Serializable, Cloneable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /** Operation outcome flag; {@code true} on success, {@code false} on failure. */
    private Boolean state;

    /** Human-readable message intended to be shown to the end user. */
    private String message;

    /** Technical message intended for logs and debugging. */
    private String internalMessage;

    /**
     * Keyed result payload. Declared {@code final} and always initialized so it
     * is never {@code null}; insertion order is preserved.
     */
    private final Map<String, Object> result = new LinkedHashMap<>();

    /**
     * No-argument constructor. Produces an {@code Answer} with no state, no
     * messages, and an empty result map.
     */
    public Answer() {
    }

    /**
     * Constructor with state and messages.
     *
     * @param state           operation state (may be {@code null})
     * @param message         message for the user (may be {@code null})
     * @param internalMessage internal message for logs/debugging (may be
     *                        {@code null})
     */
    public Answer(Boolean state, String message, String internalMessage) {
        this.state = state;
        this.message = message;
        this.internalMessage = internalMessage;
    }

    /**
     * Constructor with state, messages, and an initial result entry.
     *
     * @param state           operation state (may be {@code null})
     * @param message         message for the user (may be {@code null})
     * @param internalMessage internal message for logs/debugging (may be
     *                        {@code null})
     * @param key             result key (must not be {@code null})
     * @param result          result value (may be {@code null})
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public Answer(Boolean state, String message, String internalMessage, String key, Object result) {
        this.state = state;
        this.message = message;
        this.internalMessage = internalMessage;
        setResult(key, result);
    }

    /**
     * Copy constructor. Creates an independent {@code Answer} whose result map is
     * a shallow copy of {@code source}'s (keys and value references are copied,
     * but the values themselves are shared).
     *
     * @param source the answer to copy (must not be {@code null})
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public Answer(Answer source) {
        Objects.requireNonNull(source, "source cannot be null");
        this.state = source.state;
        this.message = source.message;
        this.internalMessage = source.internalMessage;
        this.result.putAll(source.result);
    }

    // ---------------------------------------------------------------------
    // Static factory methods
    // ---------------------------------------------------------------------

    /**
     * Creates a successful answer with no messages.
     *
     * @return a new {@code Answer} whose state is {@code true}
     */
    public static Answer ok() {
        return new Answer(Boolean.TRUE, null, null);
    }

    /**
     * Creates a successful answer carrying a user-facing message.
     *
     * @param message message for the user (may be {@code null})
     * @return a new successful {@code Answer}
     */
    public static Answer success(String message) {
        return new Answer(Boolean.TRUE, message, null);
    }

    /**
     * Creates a successful answer carrying both a user-facing and an internal
     * message.
     *
     * @param message         message for the user (may be {@code null})
     * @param internalMessage internal message for logs/debugging (may be
     *                        {@code null})
     * @return a new successful {@code Answer}
     */
    public static Answer success(String message, String internalMessage) {
        return new Answer(Boolean.TRUE, message, internalMessage);
    }

    /**
     * Creates a failed answer carrying a user-facing message.
     *
     * @param message message for the user (may be {@code null})
     * @return a new failed {@code Answer}
     */
    public static Answer failure(String message) {
        return new Answer(Boolean.FALSE, message, null);
    }

    /**
     * Creates a failed answer carrying both a user-facing and an internal message.
     *
     * @param message         message for the user (may be {@code null})
     * @param internalMessage internal message for logs/debugging (may be
     *                        {@code null})
     * @return a new failed {@code Answer}
     */
    public static Answer failure(String message, String internalMessage) {
        return new Answer(Boolean.FALSE, message, internalMessage);
    }

    // ---------------------------------------------------------------------
    // State
    // ---------------------------------------------------------------------

    /**
     * Gets the operation state.
     *
     * @return {@code true} if the operation was successful, {@code false} if it
     *         failed, or {@code null} if the state was never set
     */
    public Boolean getState() {
        return state;
    }

    /**
     * Sets the operation state.
     *
     * @param state new state (may be {@code null})
     */
    public void setState(Boolean state) {
        this.state = state;
    }

    /**
     * Tests whether this answer represents a definitive success.
     *
     * @return {@code true} only if the state is non-{@code null} and {@code true}
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(state);
    }

    /**
     * Tests whether this answer represents a definitive failure.
     *
     * @return {@code true} only if the state is non-{@code null} and {@code false}
     */
    public boolean isFailure() {
        return Boolean.FALSE.equals(state);
    }

    // ---------------------------------------------------------------------
    // Messages
    // ---------------------------------------------------------------------

    /**
     * Gets the message for the user.
     *
     * @return descriptive message of the operation (may be {@code null})
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message for the user.
     *
     * @param message new message (may be {@code null})
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the internal message for logs or debugging.
     *
     * @return internal message with technical details (may be {@code null})
     */
    public String getInternalMessage() {
        return internalMessage;
    }

    /**
     * Sets the internal message.
     *
     * @param internalMessage new internal message (may be {@code null})
     */
    public void setInternalMessage(String internalMessage) {
        this.internalMessage = internalMessage;
    }

    // ---------------------------------------------------------------------
    // Result payload
    // ---------------------------------------------------------------------

    /**
     * Gets a result from the map by key name.
     *
     * @param key the result key (may be {@code null})
     * @return the stored object, or {@code null} if not found
     */
    public Object getResult(String key) {
        return result.get(key);
    }

    /**
     * Gets a result and safely casts it to the requested type.
     * Returns {@code null} when the entry is absent or is not an instance of
     * {@code type}, so this method never throws {@link ClassCastException}.
     *
     * @param <T>  the expected result type
     * @param key  the result key (may be {@code null})
     * @param type the class object of the expected type (must not be
     *             {@code null})
     * @return the stored value cast to {@code T}, or {@code null} if absent or of
     *         a different type
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public <T> T getResult(String key, Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null");
        Object value = result.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    /**
     * Gets a result, or a fallback value when the key is absent.
     *
     * @param key          the result key (may be {@code null})
     * @param defaultValue value returned when the key is not present
     * @return the stored value, or {@code defaultValue} if the key is absent
     */
    public Object getResultOrDefault(String key, Object defaultValue) {
        return result.containsKey(key) ? result.get(key) : defaultValue;
    }

    /**
     * Tests whether a result is stored under the given key.
     *
     * @param key the result key (may be {@code null})
     * @return {@code true} if a value (including {@code null}) is mapped to
     *         {@code key}
     */
    public boolean hasResult(String key) {
        return result.containsKey(key);
    }

    /**
     * Adds or updates a result in the map.
     *
     * @param key   the result key (must not be {@code null})
     * @param value value to store (may be {@code null})
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void setResult(String key, Object value) {
        Objects.requireNonNull(key, "result key cannot be null");
        this.result.put(key, value);
    }

    /**
     * Fluent variant of {@link #setResult(String, Object)} that returns this
     * instance, enabling chained construction.
     *
     * <pre>{@code
     * return Answer.success("Saved")
     *         .with("id", entity.getId())
     *         .with("entity", entity);
     * }</pre>
     *
     * @param key   the result key (must not be {@code null})
     * @param value value to store (may be {@code null})
     * @return this {@code Answer}, for chaining
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public Answer with(String key, Object value) {
        setResult(key, value);
        return this; 
    }

    /**
     * Removes a result entry.
     *
     * @param key the result key (may be {@code null})
     * @return the previously stored value, or {@code null} if none
     */
    public Object removeResult(String key) {
        return result.remove(key);
    }

    /**
     * Gets an unmodifiable view of all stored results. Mutations must be performed
     * through {@link #setResult(String, Object)} or {@link #with(String, Object)};
     * attempting to modify the returned map throws
     * {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable view of the result map (never {@code null})
     */
    public Map<String, Object> getResults() {
        return Collections.unmodifiableMap(result);  
    }

    // ---------------------------------------------------------------------
    // Object overrides
    // ---------------------------------------------------------------------

    /**
     * Returns a string representation of this {@code Answer}.
     *
     * @return string representation of the answer
     */
    @Override
    public String toString() {
        return "Answer{" +
                "state=" + state +
                ", message='" + message + '\'' +
                ", internalMessage='" + internalMessage + '\'' +
                ", result=" + result +
                '}';
    }

    /**
     * Computes the hash code based on all fields.
     *
     * @return hash code of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(state, message, internalMessage, result); 
    }

    /**
     * Compares this {@code Answer} with another object based on all fields.
     *
     * @param obj the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Answer other = (Answer) obj;
        return Objects.equals(state, other.state) &&
                Objects.equals(message, other.message) &&
                Objects.equals(internalMessage, other.internalMessage) &&
                Objects.equals(result, other.result);
    }

    /**
     * Creates an independent copy of this {@code Answer} with a shallow copy of
     * the result map (value references are shared). Unlike a raw
     * {@link Object#clone()}, this implementation is backed by the
     * {@link #Answer(Answer) copy constructor}, so it always succeeds.
     *
     * @return a copy of this {@code Answer}
     */
    @Override
    public Answer clone() {
        return new Answer(this);
    }

    /**
     * Convenience alias for {@link #clone()} that does not require casting.
     *
     * @return an independent copy of this {@code Answer}
     */
    public Answer copy() {
        return new Answer(this);
    }

}

/**
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.persistence
 */
package io.github.dinamo541.corefx.persistence;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Singleton helper that centralizes access to a single, shared persistence
 * context (typically a JPA {@code EntityManager}) without coupling CoreFx to any
 * specific persistence provider.
 *
 * <p>
 * <b>Why this class is dependency-free.</b> CoreFx is published as a reusable
 * library and must not force its consumers to inherit a particular persistence
 * stack (Jakarta Persistence, Hibernate, EclipseLink, etc.). Referencing
 * {@code jakarta.persistence.EntityManager} directly would drag that API onto
 * every consumer's classpath. Instead this helper follows the same decoupling
 * strategy used by {@link io.github.dinamo541.corefx.navigation.FlowController}
 * and its {@code themeApplier} callback: the <em>consuming application</em>,
 * which already has a persistence provider on its classpath, supplies a
 * {@link Supplier} that produces the manager. CoreFx merely stores, lazily
 * creates, caches, and hands back that object — treating it as an opaque
 * {@link Object} — so the library itself depends only on the native Java
 * platform.
 * </p>
 *
 * <p>
 * <b>Typical wiring</b> (done once, at application startup):
 * </p>
 *
 * <pre>{@code
 * // In YOUR application, where jakarta.persistence IS available:
 * EntityManagerHelper.getInstance().initialize(
 *         () -> Persistence.createEntityManagerFactory("myPersistenceUnit")
 *                          .createEntityManager());
 * }</pre>
 *
 * <p>
 * <b>Typed retrieval</b> anywhere afterwards:
 * </p>
 *
 * <pre>{@code
 * EntityManager em = EntityManagerHelper.getInstance().getManager(EntityManager.class);
 * em.getTransaction().begin();
 * em.persist(entity);
 * em.getTransaction().commit();
 * }</pre>
 *
 * <p>
 * <b>Resilience.</b> Initialization is guarded by double-checked locking and a
 * {@code volatile} flag, mirroring {@code FlowController}. The cached manager is
 * created lazily and thread-safely on first access. If the active manager
 * becomes stale or closed, {@link #resetManager()} discards it so the next
 * access transparently rebuilds a fresh one from the supplier. {@link #close()}
 * performs a best-effort shutdown using the native {@link AutoCloseable}
 * contract, which modern persistence managers implement.
 * </p>
 *
 * @author Dominique
 * @author Sem
 * @version 2.0
 * @since 2026/06/10
 */
public final class EntityManagerHelper {

    /**
     * Holder for lazy, thread-safe singleton initialization.
     * The JVM class-loading mechanism guarantees {@code INSTANCE} is created
     * exactly once, only when {@link #getInstance()} is first called.
     */
    private static final class EntityManagerHelperHolder {
        private static final EntityManagerHelper INSTANCE = new EntityManagerHelper();
    }

    /**
     * Application-supplied factory that produces a fresh persistence manager on
     * demand. Declared {@code volatile} so its registration is visible across
     * threads. Holds {@code null} until {@link #initialize(Supplier)} succeeds.
     */
    private volatile Supplier<?> managerSupplier;

    /**
     * The lazily-created, cached manager instance handed back to callers.
     * Declared {@code volatile} to guarantee visibility of writes across threads.
     */
    private volatile Object manager;

    /** Whether {@link #initialize(Supplier)} has completed successfully. */
    private volatile boolean initialized = false;

    /**
     * Guard for the initialization block and for lazy manager creation against
     * concurrent first-time access, used together with the {@code volatile}
     * flags to implement double-checked locking.
     */
    private final Object lock = new Object();

    /**
     * Private constructor — use {@link #getInstance()} to obtain the singleton.
     */
    private EntityManagerHelper() {
    }

    /**
     * Returns the singleton instance of {@code EntityManagerHelper}.
     *
     * <p>
     * <strong>Note:</strong> {@link #initialize(Supplier)} must be called once
     * before {@link #getManager()} or {@link #getManager(Class)}.
     * </p>
     *
     * @return the single {@code EntityManagerHelper} instance
     */
    public static EntityManagerHelper getInstance() {
        return EntityManagerHelperHolder.INSTANCE;
    }

    /**
     * Registers the factory used to create the shared persistence manager.
     * Uses double-checked locking on the internal lock so the helper is
     * configured exactly once, even under concurrent access.
     *
     * <p>
     * The supplier is invoked lazily — not here — the first time a manager is
     * actually requested, and again whenever the cached manager has been
     * discarded via {@link #resetManager()} or {@link #close()}.
     * </p>
     *
     * @param managerSupplier a factory that creates a persistence manager (for
     *                        example, a JPA {@code EntityManager}); must not be
     *                        {@code null} and must not return {@code null}
     * @throws NullPointerException  if {@code managerSupplier} is {@code null}
     * @throws IllegalStateException if the helper has already been initialized
     */
    public void initialize(Supplier<?> managerSupplier) {
        Objects.requireNonNull(managerSupplier, "managerSupplier cannot be null");

        if (initialized) {
            throw new IllegalStateException(
                    "EntityManagerHelper is already initialized. Call initialize() only once.");
        }

        synchronized (lock) {
            if (initialized) {
                throw new IllegalStateException(
                        "EntityManagerHelper is already initialized. Call initialize() only once.");
            }
            this.managerSupplier = managerSupplier;
            this.initialized = true;
        }
    }

    /**
     * Internal guard invoked before any operation that requires a configured
     * supplier.
     *
     * @throws IllegalStateException if {@link #initialize(Supplier)} has not been
     *                               called yet
     */
    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "EntityManagerHelper is not initialized. Call initialize(Supplier) first.");
        }
    }

    /**
     * Returns the shared persistence manager, creating it from the registered
     * supplier on first access. The created instance is cached and returned by
     * subsequent calls until it is discarded via {@link #resetManager()} or
     * {@link #close()}.
     *
     * <p>
     * Creation is performed under the internal lock so that concurrent first-time
     * callers share a single instance rather than racing to build several.
     * </p>
     *
     * @return the shared manager as an opaque {@link Object}; cast it yourself or
     *         prefer the type-safe {@link #getManager(Class)}
     * @throws IllegalStateException if the helper has not been initialized, if the
     *                               supplier returns {@code null}, or if the
     *                               supplier fails to create a manager
     */
    public Object getManager() {
        checkInitialized();
        Object current = manager;
        if (current != null) {
            return current;
        }
        synchronized (lock) {
            if (manager == null) {
                try {
                    Object created = managerSupplier.get();
                    if (created == null) {
                        throw new IllegalStateException(
                                "The manager supplier returned null.");
                    }
                    manager = created;
                } catch (IllegalStateException ex) {
                    throw ex;
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Failed to create the persistence manager.", ex);
                }
            }
            return manager;
        }
    }

    /**
     * Returns the shared persistence manager cast to the requested type.
     * This is the recommended accessor: it lets callers work with their own
     * persistence type (for example {@code EntityManager}) without an explicit
     * cast, while keeping CoreFx itself free of that type.
     *
     * @param <T>  the expected manager type
     * @param type the class object of the expected type; must not be {@code null}
     * @return the shared manager cast to {@code T}
     * @throws NullPointerException  if {@code type} is {@code null}
     * @throws IllegalStateException if the helper is not initialized, the supplier
     *                               misbehaves, or the created manager is not an
     *                               instance of {@code type}
     */
    public <T> T getManager(Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null");
        Object current = getManager();
        if (!type.isInstance(current)) {
            throw new IllegalStateException(
                    "The persistence manager is of type "
                            + current.getClass().getName()
                            + " and cannot be viewed as " + type.getName() + ".");
        }
        return type.cast(current);
    }

    /**
     * Discards the cached manager without closing it, forcing a fresh instance to
     * be created from the supplier on the next access. Use this to recover from a
     * manager that has become stale or was closed elsewhere.
     *
     * <p>
     * Ownership of the discarded instance returns to the caller; if it still
     * holds resources, close it yourself, or use {@link #close()} instead.
     * </p>
     */
    public void resetManager() {
        synchronized (lock) {
            manager = null;
        }
    }

    /**
     * Closes the cached manager (best effort) and discards it, so the next access
     * rebuilds a fresh one from the supplier.
     *
     * <p>
     * If the cached manager implements {@link AutoCloseable} — as modern JPA
     * {@code EntityManager} instances do — its {@code close()} method is invoked.
     * Any exception raised while closing is swallowed so shutdown never fails;
     * the reference is cleared regardless. The supplier registration is
     * preserved, so the helper remains usable after closing.
     * </p>
     */
    public void close() {
        synchronized (lock) {
            Object current = manager;
            if (current instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception ignored) {
                    // Best-effort shutdown: never propagate close failures.
                }
            }
            manager = null;
        }
    }

    /**
     * Returns whether a manager instance is currently cached.
     *
     * @return {@code true} if a manager has been created and not yet discarded
     */
    public boolean hasManager() {
        return manager != null;
    }

    /**
     * Returns whether the helper has been initialized with a supplier.
     *
     * @return {@code true} if {@link #initialize(Supplier)} has completed
     *         successfully
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns a string representation of this {@code EntityManagerHelper}.
     *
     * @return string representation including initialization and cache state
     */
    @Override
    public String toString() {
        return "EntityManagerHelper{" +
                "initialized=" + initialized +
                ", hasManager=" + (manager != null) +
                '}';
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
     * Compares this {@code EntityManagerHelper} singleton with another object for
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
        throw new CloneNotSupportedException("Cloning of EntityManagerHelper is not supported");
    }

}

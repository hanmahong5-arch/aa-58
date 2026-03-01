package com.aionemu.commons.database.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO Manager — central registry for all DAO implementations.
 *
 * Concrete implementations are registered at startup by server-specific
 * registry classes (GameDAORegistry / LoginDAORegistry) via
 * {@link #registerDAO(DAO)}.  Callers retrieve implementations through
 * {@link #getDAO(Class)}.
 *
 * Thread-safety: uses a two-phase freeze pattern inspired by Spring
 * {@code DefaultListableBeanFactory.freezeConfiguration()}.
 * During registration (pre-freeze), writes are synchronized.
 * After {@link #init()}, the map becomes an unmodifiable snapshot
 * published via a volatile reference — safe for lock-free concurrent reads.
 *
 * @author SoulKeeper
 * @author Saelya
 */
public class DAOManager {

    private static final Logger log = LoggerFactory.getLogger(DAOManager.class);

    /**
     * Mutable staging map used only during the registration phase.
     * Guarded by {@code synchronized(DAOManager.class)}.
     */
    private static final Map<String, DAO> stagingMap = new HashMap<>();

    /**
     * Immutable, volatile-published snapshot created by {@link #init()}.
     * All post-init reads go through this reference with no synchronization.
     */
    private static volatile Map<String, DAO> daoMap;

    /** True after {@link #init()} has been called. */
    private static volatile boolean frozen = false;

    /**
     * Freeze the registry and publish an immutable snapshot.
     * Called after the server-specific registry has finished registration.
     */
    public static synchronized void init() {
        daoMap = Collections.unmodifiableMap(new HashMap<>(stagingMap));
        frozen = true;
        log.info("Loaded " + daoMap.size() + " DAO implementations.");
    }

    /**
     * Shut down DAOManager and clear all registrations.
     */
    public static synchronized void shutdown() {
        stagingMap.clear();
        daoMap = Collections.emptyMap();
        frozen = false;
    }

    /**
     * Retrieve a DAO implementation by its abstract base class.
     *
     * @param clazz abstract DAO class
     * @param <T>   DAO type
     * @return concrete DAO implementation
     * @throws DAONotFoundException if no implementation is registered
     * @throws IllegalStateException if called before init()
     */
    public static <T extends DAO> T getDAO(Class<T> clazz) throws DAONotFoundException {
        if (!frozen) {
            throw new IllegalStateException(
                    "DAOManager not initialized — call init() before getDAO()");
        }

        DAO result = daoMap.get(clazz.getName());

        if (result == null) {
            String s = "DAO for class " + clazz.getSimpleName() + " not implemented";
            log.error(s);
            throw new DAONotFoundException(s);
        }

        return clazz.cast(result);
    }

    /**
     * Register a concrete DAO implementation.
     * Called by GameDAORegistry / LoginDAORegistry during startup.
     *
     * @param dao concrete DAO instance
     * @throws DAOAlreadyRegisteredException if a DAO with the same className is already registered
     * @throws IllegalStateException if called after init() (registry is frozen)
     */
    public static void registerDAO(DAO dao) {
        synchronized (DAOManager.class) {
            if (frozen) {
                throw new IllegalStateException(
                        "DAOManager is frozen — cannot register DAO after init()");
            }

            String key = dao.getClassName();
            if (stagingMap.containsKey(key)) {
                throw new DAOAlreadyRegisteredException(
                        "DAO " + key + " already registered by " +
                        stagingMap.get(key).getClass().getName() +
                        ". Cannot override with " + dao.getClass().getName() + ".");
            }
            stagingMap.put(key, dao);
        }

        if (log.isDebugEnabled()) {
            log.debug("DAO " + dao.getClassName() + " registered.");
        }
    }

    private DAOManager() {
    }
}

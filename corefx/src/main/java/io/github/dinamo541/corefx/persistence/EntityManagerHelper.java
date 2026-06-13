/**
 * CoreFx - JavaFX utility library
 * Author: Dominique Mariano Q.C.
 * Date: 10 jun 2026
 * Package: io.github.dinamo541.corefx.persistence
 */
package io.github.dinamo541.corefx.persistence;

import io.github.dinamo541.corefx.navigation.FlowController;

/**
 *
 * @author Dominique
 * @version 1.0
 * @since 2026/06/10
 */
public class EntityManagerHelper {

    private static final class EntityManagerHelperHolder {
        private static final EntityManagerHelper INSTANCE = new EntityManagerHelper();
    }

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private EntityManagerHelper() {
    }

    static {
        try {
            emf = Persistence.createEntityManagerFactory("cr.ac.una_Soulward_jar_1.0PU");
            em = emf.createEntityManager();
        } catch (ExceptionInInitializerError e) {
            throw e;
        }
    }

    public static EntityManagerHelper getInstance() {

        return EntityManagerHelperHolder.INSTANCE;
    }

    public static EntityManager getManager() {
        if (em == null) {
            emf = Persistence.createEntityManagerFactory("cr.ac.una_Soulward_jar_1.0PU");
            em = emf.createEntityManager();
        }
        return em;
    }
}

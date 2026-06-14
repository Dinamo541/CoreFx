/*
 * CoreFx - JavaFX utility library
 * Tests for AppContext
 * Package: io.github.dinamo541.corefx.navigation
 */
package io.github.dinamo541.corefx.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AppContext}. As {@code AppContext} is a process-wide
 * singleton, each test clears the store before and after running to stay
 * isolated.
 */
class AppContextTest {

    private final AppContext context = AppContext.getInstance();

    @BeforeEach
    void reset() {
        context.clear();
    }

    @AfterEach
    void cleanup() {
        context.clear();
    }

    // ---------------------------------------------------------------------
    // Singleton
    // ---------------------------------------------------------------------

    @Test
    void getInstanceReturnsSameInstance() {
        assertSame(AppContext.getInstance(), AppContext.getInstance());
    }

    // ---------------------------------------------------------------------
    // Write / read round trips
    // ---------------------------------------------------------------------

    @Test
    void putThenGet() {
        context.put("user", "Ada");
        String user = context.get("user");
        assertEquals("Ada", user);
    }

    @Test
    void getInfersTargetType() {
        context.put("count", 7);
        int count = context.get("count");
        assertEquals(7, count);
    }

    @Test
    void getMissingReturnsNull() {
        assertNull(context.get("missing"));
    }

    @Test
    void getOrDefaultFallsBack() {
        context.put("present", "v");
        assertEquals("v", context.getOrDefault("present", "fallback"));
        assertEquals("fallback", context.getOrDefault("absent", "fallback"));
    }

    @Test
    void putIfAbsentDoesNotOverwrite() {
        assertNull(context.putIfAbsent("k", "first"));
        assertEquals("first", context.putIfAbsent("k", "second"));
        assertEquals("first", context.get("k"));
    }

    @Test
    void containsReflectsPresence() {
        assertFalse(context.contains("k"));
        context.put("k", "v");
        assertTrue(context.contains("k"));
    }

    @Test
    void removeDeletesEntry() {
        context.put("k", "v");
        context.remove("k");
        assertFalse(context.contains("k"));
    }

    @Test
    void clearEmptiesStore() {
        context.put("a", 1);
        context.put("b", 2);
        assertEquals(2, context.size());
        context.clear();
        assertTrue(context.isEmpty());
        assertEquals(0, context.size());
    }

    @Test
    void sizeAndIsEmptyTrackEntries() {
        assertTrue(context.isEmpty());
        context.put("a", 1);
        assertFalse(context.isEmpty());
        assertEquals(1, context.size());
    }

    // ---------------------------------------------------------------------
    // Contracts
    // ---------------------------------------------------------------------

    @Test
    void nullValueIsRejected() {
        assertThrows(NullPointerException.class, () -> context.put("k", null));
        assertThrows(NullPointerException.class, () -> context.putIfAbsent("k", null));
    }

    @Test
    void blankOrNullKeyIsRejectedEverywhere() {
        assertThrows(IllegalArgumentException.class, () -> context.put("  ", "v"));
        assertThrows(IllegalArgumentException.class, () -> context.put(null, "v"));
        assertThrows(IllegalArgumentException.class, () -> context.get(""));
        assertThrows(IllegalArgumentException.class, () -> context.getOrDefault(" ", "d"));
        assertThrows(IllegalArgumentException.class, () -> context.contains(null));
        assertThrows(IllegalArgumentException.class, () -> context.remove(""));
        assertThrows(IllegalArgumentException.class, () -> context.putIfAbsent("  ", "v"));
    }

    @Test
    void wrongTypeCastThrowsAtUseSite() {
        context.put("count", 7);
        assertThrows(ClassCastException.class, () -> {
            String wrong = context.get("count");
            // Force the implicit cast to actually run.
            wrong.length();
        });
    }
}

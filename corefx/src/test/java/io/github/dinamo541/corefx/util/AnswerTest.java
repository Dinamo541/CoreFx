/*
 * CoreFx - JavaFX utility library
 * Tests for Answer
 * Package: io.github.dinamo541.corefx.util
 */
package io.github.dinamo541.corefx.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Answer}. Covers the factory methods, the fluent result
 * payload, the key null-safety contract, and value-object semantics.
 */
class AnswerTest {

    // ---------------------------------------------------------------------
    // Factory methods and state
    // ---------------------------------------------------------------------

    @Test
    void okIsSuccess() {
        Answer answer = Answer.ok();
        assertTrue(answer.isSuccess());
        assertFalse(answer.isFailure());
        assertEquals(Boolean.TRUE, answer.getState());
    }

    @Test
    void successCarriesMessage() {
        Answer answer = Answer.success("done");
        assertTrue(answer.isSuccess());
        assertEquals("done", answer.getMessage());

        Answer detailed = Answer.success("done", "internal detail");
        assertEquals("internal detail", detailed.getInternalMessage());
    }

    @Test
    void failureCarriesMessage() {
        Answer answer = Answer.failure("nope");
        assertTrue(answer.isFailure());
        assertFalse(answer.isSuccess());
        assertEquals("nope", answer.getMessage());
    }

    @Test
    void nullStateIsNeitherSuccessNorFailure() {
        Answer answer = new Answer();
        assertNull(answer.getState());
        assertFalse(answer.isSuccess());
        assertFalse(answer.isFailure());
    }

    // ---------------------------------------------------------------------
    // Result payload
    // ---------------------------------------------------------------------

    @Test
    void withChainsAndStoresResults() {
        Answer answer = Answer.success("saved")
                .with("id", 7)
                .with("name", "Ada");

        assertEquals(7, answer.getResult("id"));
        assertEquals("Ada", answer.getResult("name"));
        assertTrue(answer.hasResult("id"));
        assertFalse(answer.hasResult("missing"));
    }

    @Test
    void typedGetSafelyCastsAndNullsOnMismatch() {
        Answer answer = Answer.ok().with("count", 42);
        assertEquals(Integer.valueOf(42), answer.getResult("count", Integer.class));
        // Wrong type must return null rather than throwing ClassCastException.
        assertNull(answer.getResult("count", String.class));
        assertNull(answer.getResult("absent", Integer.class));
    }

    @Test
    void getResultOrDefault() {
        Answer answer = Answer.ok().with("present", "v");
        assertEquals("v", answer.getResultOrDefault("present", "fallback"));
        assertEquals("fallback", answer.getResultOrDefault("absent", "fallback"));
    }

    @Test
    void removeResultReturnsPrevious() {
        Answer answer = Answer.ok().with("k", "v");
        assertEquals("v", answer.removeResult("k"));
        assertFalse(answer.hasResult("k"));
        assertNull(answer.removeResult("k"));
    }

    @Test
    void nullKeyIsRejectedAcrossTheMapApi() {
        Answer answer = Answer.ok();
        assertThrows(NullPointerException.class, () -> answer.setResult(null, "v"));
        assertThrows(NullPointerException.class, () -> answer.with(null, "v"));
        assertThrows(NullPointerException.class, () -> answer.getResult(null));
        assertThrows(NullPointerException.class, () -> answer.getResult(null, String.class));
        assertThrows(NullPointerException.class, () -> answer.getResultOrDefault(null, "d"));
        assertThrows(NullPointerException.class, () -> answer.hasResult(null));
        assertThrows(NullPointerException.class, () -> answer.removeResult(null));
    }

    @Test
    void getResultsIsUnmodifiable() {
        Answer answer = Answer.ok().with("k", "v");
        assertThrows(UnsupportedOperationException.class,
                () -> answer.getResults().put("x", "y"));
        assertEquals(1, answer.getResults().size());
    }

    @Test
    void resultsPreserveInsertionOrder() {
        Answer answer = Answer.ok().with("b", 1).with("a", 2).with("c", 3);
        assertEquals("[b, a, c]", answer.getResults().keySet().toString());
    }

    // ---------------------------------------------------------------------
    // Value-object semantics
    // ---------------------------------------------------------------------

    @Test
    void copyAndCloneAreIndependent() {
        Answer original = Answer.success("ok").with("id", 1);
        Answer copy = original.copy();
        Answer cloned = original.clone();

        assertEquals(original, copy);
        assertEquals(original, cloned);
        assertNotSame(original, copy);

        // Mutating the copy must not affect the original.
        copy.with("id", 999);
        assertEquals(1, original.getResult("id"));
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        Answer a = Answer.success("ok").with("id", 1);
        Answer b = Answer.success("ok").with("id", 1);
        Answer c = Answer.success("ok").with("id", 2);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
    }

    @Test
    void copyConstructorRejectsNull() {
        assertThrows(NullPointerException.class, () -> new Answer(null));
    }
}

package me.mrgeneralq.sleepmost.core.statics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerVersionTest {

    @Test
    public void parsesAll26xVersions() {
        assertEquals(ServerVersion.V26_X, ServerVersion.parse("26.1.1-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V26_X, ServerVersion.parse("26.1.2-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V26_X, ServerVersion.parse("26.2-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V26_X, ServerVersion.parse("26.3-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V26_X, ServerVersion.parse("26.3.build.248-stable"));
        assertEquals(ServerVersion.V26_X, ServerVersion.parse("git-Paper-123 (MC: 26.3)"));
        // Later 26.x releases keep the tested API family.
        assertEquals(ServerVersion.V26_X, ServerVersion.parse("26.4-R0.1-SNAPSHOT"));
    }

    @Test
    public void detectsNewerMajorVersions() {
        assertEquals(ServerVersion.NEWER, ServerVersion.parse("27.1-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.NEWER, ServerVersion.parse("git-Purpur-123 (MC: 28.0)"));
    }

    @Test
    public void rejectsUnsupportedVersions() {
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("1.8.8-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("1.20.6-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("1.21.11-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("26.0-R0.1-SNAPSHOT"));
        // 26.1.0 and bare 26.1 are below the 26.1.1 minimum.
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("26.1-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("26.1.0-R0.1-SNAPSHOT"));
    }

    @Test
    public void supportedRangeStartsAt2611() {
        assertTrue(ServerVersion.V26_X.isSupported());
        assertFalse(ServerVersion.NEWER.isSupported());
        assertFalse(ServerVersion.UNKNOWN.isSupported());

        assertFalse(ServerVersion.V26_X.isNewerThanTested());
        assertTrue(ServerVersion.NEWER.isNewerThanTested());

        assertEquals("26.x", ServerVersion.V26_X.getName());
        assertEquals("newer", ServerVersion.NEWER.getName());
        assertEquals("unknown", ServerVersion.UNKNOWN.getName());
    }

    @Test
    public void rejectsGarbage() {
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse(null));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse(""));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("not-a-version"));
    }
}
